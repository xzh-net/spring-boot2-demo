package net.xzh.geo.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import lombok.RequiredArgsConstructor;
import net.xzh.geo.config.AmapProperties;
import net.xzh.geo.model.GeoCodeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class AmapBatchGeoService implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AmapBatchGeoService.class);

    // ========== 配额/限流配置 ==========
    private static final int BATCH_SIZE = 20;
    private static final int MAX_CONCURRENT = 2;
    private static final int DELAY_MS = 300;
    private static final int MAX_REQUESTS_PER_RUN = 2000;
    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_MS = 1000;

    private final JdbcTemplate jdbcTemplate;
    private final AmapProperties amapProperties;

    private final ExecutorService executor = Executors.newFixedThreadPool(MAX_CONCURRENT);
    private final AtomicInteger requestCount = new AtomicInteger(0);

    private int maxRequestsPerRun = MAX_REQUESTS_PER_RUN;

    @Override
    public void run(String... args) {
        if (args.length > 0 && "geocode".equals(args[0])) {
            if (args.length > 1) {
                try {
                    maxRequestsPerRun = Integer.parseInt(args[1]);
                } catch (NumberFormatException ignored) {}
            } else {
                maxRequestsPerRun = MAX_REQUESTS_PER_RUN;
            }
            processAll();
        } else {
            log.info("启动参数未包含 'geocode'，跳过逆地理编码。使用: java -jar xxx.jar geocode [请求数量]");
        }
    }

    public void processAll() {
        requestCount.set(0);
        log.info("=== 开始批量逆地理编码 (单次上限 {} 请求) ===", maxRequestsPerRun);
        try {
            processCodedTable();
            if (requestCount.get() < maxRequestsPerRun) processOldUncodedTable();
            if (requestCount.get() < maxRequestsPerRun) processV1UncodedTable();
            log.info("=== 批量逆地理编码完成，本次共请求 {} 次 ===", requestCount.get());
        } catch (Exception e) {
            log.error("批量逆地理编码失败", e);
            throw new RuntimeException(e);
        } finally {
            executor.shutdown();
            try { executor.awaitTermination(2, TimeUnit.MINUTES); } catch (InterruptedException ignored) {}
        }
    }

    private void processCodedTable() {
        log.info("--- 处理 tmp_all_coded_land ---");
        String sql = "SELECT code, lng, lat FROM tmp_all_coded_land WHERE adcode IS NULL AND lng IS NOT NULL AND lat IS NOT NULL";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
        log.info("待处理: {} 条", rows.size());
        processBatch(rows, (code, adcode, formattedAddr, country, province, city, district, township, citycode, towncode) ->
                jdbcTemplate.update("UPDATE tmp_all_coded_land SET adcode=?, formatted_address=?, country=?, province=?, city=?, district=?, township=?, citycode=?, towncode=? WHERE code=?",
                        adcode, formattedAddr, country, province, city, district, township, citycode, towncode, code), "code");
    }

    private void processOldUncodedTable() {
        log.info("--- 处理 tmp_old_uncoded_land ---");
        String sql = "SELECT id, lng, lat FROM tmp_old_uncoded_land WHERE adcode IS NULL AND lng IS NOT NULL AND lat IS NOT NULL";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
        log.info("待处理: {} 条", rows.size());
        processBatch(rows, (id, adcode, formattedAddr, country, province, city, district, township, citycode, towncode) ->
                jdbcTemplate.update("UPDATE tmp_old_uncoded_land SET adcode=?, formatted_address=?, country=?, province=?, city=?, district=?, township=?, citycode=?, towncode=? WHERE id=?",
                        adcode, formattedAddr, country, province, city, district, township, citycode, towncode, id), "id");
    }

    private void processV1UncodedTable() {
        log.info("--- 处理 tmp_v1_uncoded_land ---");
        String sql = "SELECT id, lng, lat FROM tmp_v1_uncoded_land WHERE adcode IS NULL AND lng IS NOT NULL AND lat IS NOT NULL";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
        log.info("待处理: {} 条", rows.size());
        processBatch(rows, (id, adcode, formattedAddr, country, province, city, district, township, citycode, towncode) ->
                jdbcTemplate.update("UPDATE tmp_v1_uncoded_land SET adcode=?, formatted_address=?, country=?, province=?, city=?, district=?, township=?, citycode=?, towncode=? WHERE id=?",
                        adcode, formattedAddr, country, province, city, district, township, citycode, towncode, id), "id");
    }

    @FunctionalInterface
    interface UpdateConsumer {
        void accept(String key, String adcode, String formattedAddress,
                    String country, String province, String city, String district,
                    String township, String citycode, String towncode);
    }

    private void processBatch(List<Map<String, Object>> rows, UpdateConsumer updater, String keyCol) {
        if (CollUtil.isEmpty(rows)) { log.info("无数据跳过"); return; }
        log.info("开始分批处理，共 {} 条", rows.size());

        for (int i = 0; i < rows.size(); i += BATCH_SIZE) {
            if (requestCount.get() >= maxRequestsPerRun) {
                log.warn("达到单次运行上限 {}，剩余 {} 条未处理", maxRequestsPerRun, rows.size() - i);
                break;
            }
            int end = Math.min(i + BATCH_SIZE, rows.size());
            List<Map<String, Object>> batch = rows.subList(i, end);
            processSingleBatch(batch, updater, keyCol);
            if (i + BATCH_SIZE < rows.size()) sleep(DELAY_MS);
        }
        log.info("处理完成，累计请求 {} 次", requestCount.get());
    }

    private void processSingleBatch(List<Map<String, Object>> batch, UpdateConsumer updater, String keyCol) {
        StringBuilder locations = new StringBuilder();
        List<String> keys = new ArrayList<>();

        for (Map<String, Object> row : batch) {
            String key = String.valueOf(row.get(keyCol));
            BigDecimal lng = (BigDecimal) row.get("lng");
            BigDecimal lat = (BigDecimal) row.get("lat");
            if (lng == null || lat == null) continue;
            if (locations.length() > 0) locations.append("|");
            locations.append(lng.stripTrailingZeros().toPlainString()).append(",").append(lat.stripTrailingZeros().toPlainString());
            keys.add(key);
        }
        if (keys.isEmpty()) return;

        String response = callAmapWithRetry(locations.toString(), keys.size());
        if (response == null) return;

        GeoCodeResponse resp = JSON.parseObject(response, GeoCodeResponse.class);
        if (resp == null || !"1".equals(resp.getStatus()) || resp.getRegeocodes() == null) {
            log.warn("批量请求失败: {}", resp != null ? resp.getInfo() : "null");
            return;
        }

        int success = 0;
        for (int i = 0; i < Math.min(keys.size(), resp.getRegeocodes().size()); i++) {
            GeoCodeResponse.Regeocode re = resp.getRegeocodes().get(i);
            if (re != null && re.getAddressComponent() != null && re.getAddressComponent().getAdcode() != null) {
                GeoCodeResponse.AddressComponent ac = re.getAddressComponent();
                updater.accept(keys.get(i),
                        ac.getAdcode(),
                        re.getFormattedAddress(),
                        ac.getCountry(),
                        ac.getProvince(),
                        ac.getCity(),
                        ac.getDistrict(),
                        ac.getTownship(),
                        ac.getCitycode(),
                        ac.getTowncode());
                success++;
            }
        }
        log.info("批次成功回写 {}/{} 条，累计请求 {}", success, keys.size(), requestCount.get());
    }

    private String callAmapWithRetry(String locations, int batchSize) {
        String url = amapProperties.getBaseUrl() + "/geocode/regeo";

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            if (requestCount.get() >= maxRequestsPerRun) {
                log.warn("配额上限，停止重试");
                return null;
            }

            try {
                // 使用 JDK 原生 URLEncoder 编码 location 参数（管道符 | 需编码为 %7C）
                String encodedLocation = URLEncoder.encode(locations, StandardCharsets.UTF_8.toString());
                String fullUrl = url + "?key=" + amapProperties.getKey()
                        + "&location=" + encodedLocation
                        + "&extensions=all&batch=true&radius=1000&roadlevel=1";

                String response = HttpUtil.get(fullUrl);

                requestCount.incrementAndGet();
                GeoCodeResponse resp = JSON.parseObject(response, GeoCodeResponse.class);

                if (resp != null && "1".equals(resp.getStatus())) {
                    return response;
                }

                if (resp != null && isQuotaError(resp.getInfocode())) {
                    log.error("高德配额/频率限制 [{}]: {}，停止重试", resp.getInfocode(), resp.getInfo());
                    return null;
                }

                log.warn("第{}次调用失败 [{}]: {}", attempt, resp != null ? resp.getInfocode() : "parse_fail", resp != null ? resp.getInfo() : "null");

            } catch (Exception e) {
                log.warn("第{}次调用异常: {}", attempt, e.getMessage());
            }

            if (attempt < MAX_RETRIES) sleep(RETRY_DELAY_MS * attempt);
        }
        return null;
    }

    private boolean isQuotaError(String infocode) {
        return "10001".equals(infocode) || "10002".equals(infocode) || "10003".equals(infocode) || "10004".equals(infocode);
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}