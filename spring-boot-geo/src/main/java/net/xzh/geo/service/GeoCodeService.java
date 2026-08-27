package net.xzh.geo.service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;

import cn.hutool.http.HttpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.xzh.geo.config.AmapProperties;
import net.xzh.geo.model.GeoCodeResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeoCodeService {

	private static final int BATCH_SIZE = 20;
	private static final int DELAY_MS = 300;
	private static final int MAX_RETRIES = 3;
	private static final int RETRY_DELAY_MS = 1000;

	private final AmapProperties amapProperties;

	// 硬编码121个坐标: 经度,纬度 (黑吉辽 + 中亚错误坐标)
	private static final List<String> COORDINATES = Arrays.asList(
			// ===== 黑龙江 =====
			"126.642464,45.756967", "126.553030,45.792870", "126.632810,45.748980", "127.001370,47.353160",
			"127.028910,47.356830", "127.204610,47.209220", "127.428130,47.448830", "127.592880,47.526990",
			"127.772170,47.633930", "127.984580,47.736420", "128.206190,47.843990", "128.437150,47.956640",
			"128.677570,48.074370", "128.927590,48.197200", "129.187340,48.325150", "129.456970,48.458240",
			"129.736630,48.596500", "130.026490,48.739970", "130.326740,48.888680", "130.637690,49.042690",
			"130.959660,49.202060", "131.292990,49.366840", "131.638040,49.537120", "131.995170,49.713000",
			"132.364780,49.894620", "132.747260,50.082120", "133.143030,50.275690", "133.552560,50.475550",
			"133.976330,50.682020", "134.414850,50.895450", "134.868640,51.116200", "135.338250,51.344760",
			"125.324020,43.886841", "125.424020,43.886841", "125.524020,43.886841", "125.624020,43.886841",
			"125.724020,43.886841", "125.824020,43.886841", "125.924020,43.886841", "126.024020,43.886841",
			"126.124020,43.886841", "126.224020,43.886841", "126.324020,43.886841", "126.424020,43.886841",
			"126.524020,43.886841", "126.624020,43.886841", "126.724020,43.886841", "126.824020,43.886841",
			"126.924020,43.886841", "127.024020,43.886841", "127.124020,43.886841", "127.224020,43.886841",
			"127.324020,43.886841", "127.424020,43.886841", "127.524020,43.886841", "127.624020,43.886841",
			"127.724020,43.886841", "127.824020,43.886841", "127.924020,43.886841", "128.024020,43.886841",
			"128.124020,43.886841", "128.224020,43.886841", "128.324020,43.886841", "128.424020,43.886841",
			"128.524020,43.886841", "128.624020,43.886841", "128.724020,43.886841", "128.824020,43.886841",
			"128.924020,43.886841", "129.024020,43.886841", "129.124020,43.886841", "129.224020,43.886841",
			"129.324020,43.886841",
			// ===== 辽宁 =====
			"123.431474,41.805698", "123.531474,41.805698", "123.631474,41.805698",
			"123.731474,41.805698", "123.831474,41.805698", "123.931474,41.805698", "124.031474,41.805698",
			"124.131474,41.805698", "124.231474,41.805698", "124.331474,41.805698", "124.431474,41.805698",
			"124.531474,41.805698", "124.631474,41.805698", "124.731474,41.805698", "124.831474,41.805698",
			"124.931474,41.805698", "125.031474,41.805698", "125.131474,41.805698", "125.231474,41.805698",
			"125.331474,41.805698", "125.431474,41.805698", "125.531474,41.805698", "125.631474,41.805698",
			"125.731474,41.805698", "125.831474,41.805698", "125.931474,41.805698", "126.031474,41.805698",
			"126.131474,41.805698",
			// ===== 中亚错误坐标(保留) =====
			"81.350380,48.272880", "80.700380,48.566880", "80.050380,48.860880", "79.400380,49.154880",
			"78.750380,49.448880", "78.100380,49.742880", "77.450380,50.036880", "76.800380,50.330880",
			"76.150380,50.624880", "75.500380,50.918880", "74.850380,51.212880", "74.200380,51.506880",
			"73.550380,51.800880", "72.900380,52.094880", "72.250380,52.388880", "71.600380,52.682880",
			"70.950380,52.976880", "70.300380,53.270880", "69.650380,53.564880", "69.000380,53.858880");

	// ========== 单次调用 ==========

	public GeoCodeResponse getAddressByLocation(String longitude, String latitude) {
		String location = longitude + "," + latitude;
		String url = amapProperties.getBaseUrl() + "/geocode/regeo";

		Map<String, Object> params = new HashMap<>();
		params.put("key", amapProperties.getKey());
		params.put("location", location);
		params.put("extensions", "all");
		params.put("radius", "1000");
		params.put("roadlevel", "1");

		try {
			String response = HttpUtil.get(url, params);
			return JSON.parseObject(response, GeoCodeResponse.class);
		} catch (Exception e) {
			log.warn("单次查询异常: {}", e.getMessage());
			return null;
		}
	}

	// ========== 批量调用 ==========

	public GeoCodeResponse batchRegeo() {
		log.info("开始批量逆地理编码，共 {} 个坐标，分 {} 批处理",
				COORDINATES.size(), (COORDINATES.size() + BATCH_SIZE - 1) / BATCH_SIZE);

		List<GeoCodeResponse.Regeocode> allResults = new ArrayList<>();

		for (int i = 0; i < COORDINATES.size(); i += BATCH_SIZE) {
			int end = Math.min(i + BATCH_SIZE, COORDINATES.size());
			List<String> batch = COORDINATES.subList(i, end);
			int batchNum = i / BATCH_SIZE + 1;

			processBatch(batch, allResults, batchNum);

			if (i + BATCH_SIZE < COORDINATES.size()) {
				sleep(DELAY_MS);
			}
		}

		log.info("批量逆地理编码完成，坐标数 {}，返回结果数 {}", COORDINATES.size(), allResults.size());

		GeoCodeResponse response = new GeoCodeResponse();
		response.setStatus("1");
		response.setInfo("OK");
		response.setRegeocodes(allResults);
		return response;
	}

	private void processBatch(List<String> batch, List<GeoCodeResponse.Regeocode> results, int batchNum) {
		String locations = String.join("|", batch);

		Map<String, Object> params = new HashMap<>();
		params.put("key", amapProperties.getKey());
		try {
			params.put("location", URLEncoder.encode(locations, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		params.put("extensions", "all");
		params.put("batch", "true");
		params.put("radius", "1000");
		params.put("roadlevel", "1");

		String response = callBatchWithRetry(params, batchNum);
		if (response == null) {
			return;
		}

		GeoCodeResponse resp = JSON.parseObject(response, GeoCodeResponse.class);
		if (resp != null && "1".equals(resp.getStatus()) && resp.getRegeocodes() != null) {
			List<GeoCodeResponse.Regeocode> valid = new ArrayList<>();
			for (GeoCodeResponse.Regeocode r : resp.getRegeocodes()) {
				if (r.getFormattedAddress() != null && !r.getFormattedAddress().isEmpty()
						&& !"[]".equals(r.getFormattedAddress())) {
					valid.add(r);
				}
			}
			results.addAll(valid);
			log.info("批次 {} 成功，发送 {} 个坐标，有效 {} 条，过滤无效 {} 条",
					batchNum, batch.size(), valid.size(), resp.getRegeocodes().size() - valid.size());
		} else {
			log.warn("批次 {} 返回数据为空，跳过坐标: {}", batchNum, batch);
		}
	}

	private String callBatchWithRetry(Map<String, Object> params, int batchNum) {
		String url = amapProperties.getBaseUrl() + "/geocode/regeo";
		String prefix = "批次 " + batchNum + " ";

		for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
			try {
				String response = HttpUtil.get(url, params);
				GeoCodeResponse resp = JSON.parseObject(response, GeoCodeResponse.class);

				if (resp != null && "1".equals(resp.getStatus())) {
					if (resp.getRegeocodes() != null && !resp.getRegeocodes().isEmpty()) {
						return response;
					}
					log.warn("{}调用成功返回空对象，跳过", prefix);
					return null;
				}

				if (resp != null && isQuotaError(resp.getInfocode())) {
					log.error("{}高德配额/频率限制 [{}]: {}，停止重试", prefix, resp.getInfocode(), resp.getInfo());
					return null;
				}

				log.warn("{}第{}次调用失败 [{}]: {}", prefix, attempt,
						resp != null ? resp.getInfocode() : "parse_fail",
						resp != null ? resp.getInfo() : "null");

			} catch (Exception e) {
				log.warn("{}第{}次调用异常: {}", prefix, attempt, e.getMessage());
			}

			if (attempt < MAX_RETRIES)
				sleep(RETRY_DELAY_MS * attempt);
		}
		return null;
	}

	// ========== 工具方法 ==========

	private boolean isQuotaError(String infocode) {
		return "10001".equals(infocode) || "10002".equals(infocode)
				|| "10003".equals(infocode) || "10004".equals(infocode);
	}

	private void sleep(long ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException ignored) {
		}
	}
}
