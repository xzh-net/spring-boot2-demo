package net.xzh.geo.service;

import java.util.List;
import java.util.Map;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class LandDataExtractService implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        if (args.length > 0 && "extract".equals(args[0])) {
            extractAndBuildTempTables();
        } else {
            log.info("启动参数未包含 'extract'，跳过数据抽取。使用: java -jar xxx.jar extract");
        }
    }

    public void extractAndBuildTempTables() {
        log.info("=== 开始抽取地块数据并构建临时表 ===");

        try {
            // 1. 先探测 JSON 格式
            inspectJsonFormat();

            // 2. 创建临时表并抽取数据
            buildTempTables();

            // 3. 验证统计
            verifyStats();

            log.info("=== 数据抽取完成 ===");
        } catch (Exception e) {
            log.error("数据抽取失败", e);
            throw new RuntimeException(e);
        }
    }

    private void inspectJsonFormat() {
        log.info("--- 探测 t_farm_land land_jwd JSON 格式 ---");
        List<Map<String, Object>> samples = jdbcTemplate.queryForList(
            "SELECT land_jwd, land_jwd_gcj02 FROM t_farm_land " +
            "WHERE del_flag = 0 AND land_jwd IS NOT NULL AND land_jwd <> '' LIMIT 3"
        );
        for (Map<String, Object> row : samples) {
            log.info("land_jwd: {}", row.get("land_jwd"));
            log.info("land_jwd_gcj02: {}", row.get("land_jwd_gcj02"));
        }
    }

    private void buildTempTables() {
        // 1. 创建三张目标表
        log.info("创建 tmp_all_coded_land...");
        jdbcTemplate.execute("DROP TABLE IF EXISTS tmp_all_coded_land");
        jdbcTemplate.execute("CREATE TABLE tmp_all_coded_land (src VARCHAR(20) NOT NULL, code VARCHAR(100) NOT NULL, lng DECIMAL(18,10), lat DECIMAL(18,10), src_id VARCHAR(64), adcode VARCHAR(12), formatted_address VARCHAR(500), country VARCHAR(50), province VARCHAR(50), city VARCHAR(50), district VARCHAR(50), township VARCHAR(100), citycode VARCHAR(10), towncode VARCHAR(12), PRIMARY KEY (code)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

        log.info("创建 tmp_old_uncoded_land...");
        jdbcTemplate.execute("DROP TABLE IF EXISTS tmp_old_uncoded_land");
        jdbcTemplate.execute("CREATE TABLE tmp_old_uncoded_land (id VARCHAR(36) NOT NULL PRIMARY KEY, lng DECIMAL(18,10), lat DECIMAL(18,10), adcode VARCHAR(12), formatted_address VARCHAR(500), country VARCHAR(50), province VARCHAR(50), city VARCHAR(50), district VARCHAR(50), township VARCHAR(100), citycode VARCHAR(10), towncode VARCHAR(12), platform_code VARCHAR(64)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

        log.info("创建 tmp_v1_uncoded_land...");
        jdbcTemplate.execute("DROP TABLE IF EXISTS tmp_v1_uncoded_land");
        jdbcTemplate.execute("CREATE TABLE tmp_v1_uncoded_land (id BIGINT NOT NULL PRIMARY KEY, lng DECIMAL(18,10), lat DECIMAL(18,10), adcode VARCHAR(12), formatted_address VARCHAR(500), country VARCHAR(50), province VARCHAR(50), city VARCHAR(50), district VARCHAR(50), township VARCHAR(100), citycode VARCHAR(10), towncode VARCHAR(12), data_code VARCHAR(100)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

        // 2. 创建中间去重表：老系统有编码（按 platform_code 去重，取 update_time 最新）
        log.info("创建 tmp_old_coded_dedup...");
        jdbcTemplate.execute("DROP TABLE IF EXISTS tmp_old_coded_dedup");
        jdbcTemplate.execute("CREATE TABLE tmp_old_coded_dedup (src VARCHAR(20) NOT NULL, code VARCHAR(100) NOT NULL, lng DECIMAL(18,10), lat DECIMAL(18,10), src_id VARCHAR(64), PRIMARY KEY (code)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        int oldCoded = jdbcTemplate.update(
            "INSERT INTO tmp_old_coded_dedup (src, code, lng, lat, src_id) " +
            "SELECT 't_farm_land', t1.platform_code, " +
            "       CAST(JSON_UNQUOTE(JSON_EXTRACT(t1.land_jwd_gcj02, '$.properties.center.lng')) AS DECIMAL(18,10)), " +
            "       CAST(JSON_UNQUOTE(JSON_EXTRACT(t1.land_jwd_gcj02, '$.properties.center.lat')) AS DECIMAL(18,10)), " +
            "       t1.id " +
            "FROM t_farm_land t1 " +
            "JOIN (SELECT platform_code, MAX(update_time) max_t FROM t_farm_land WHERE del_flag=0 AND platform_code IS NOT NULL AND platform_code<>'' AND platform_code<>'0' AND land_jwd_gcj02 IS NOT NULL AND land_jwd_gcj02<>'' GROUP BY platform_code) t2 " +
            "  ON t1.platform_code=t2.platform_code AND t1.update_time=t2.max_t " +
            "WHERE t1.del_flag=0 AND JSON_EXTRACT(t1.land_jwd_gcj02, '$.properties.center.lng') IS NOT NULL"
        );
        log.info("老系统有编码去重后: {} 条", oldCoded);

        // 3. 创建中间去重表：v1 有编码（按 data_code 去重，取 update_time 最新）
        log.info("创建 tmp_v1_coded_dedup...");
        jdbcTemplate.execute("DROP TABLE IF EXISTS tmp_v1_coded_dedup");
        jdbcTemplate.execute("CREATE TABLE tmp_v1_coded_dedup (src VARCHAR(20) NOT NULL, code VARCHAR(100) NOT NULL, lng DECIMAL(18,10), lat DECIMAL(18,10), src_id VARCHAR(64), PRIMARY KEY (code)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        int v1Coded = jdbcTemplate.update(
            "INSERT INTO tmp_v1_coded_dedup (src, code, lng, lat, src_id) " +
            "SELECT 'hr_bd_land', t1.data_code, CAST(t1.longitude AS DECIMAL(18,10)), CAST(t1.latitude AS DECIMAL(18,10)), t1.id " +
            "FROM hr_bd_land t1 " +
            "JOIN (SELECT data_code, MAX(update_time) max_t FROM hr_bd_land WHERE deleted=0 AND data_code IS NOT NULL AND data_code<>'' AND data_code<>'0' AND longitude IS NOT NULL AND latitude IS NOT NULL AND longitude<>'' AND latitude<>'' GROUP BY data_code) t2 " +
            "  ON t1.data_code=t2.data_code AND t1.update_time=t2.max_t " +
            "WHERE t1.deleted=0"
        );
        log.info("v1 有编码去重后: {} 条", v1Coded);

        // 4. 合并：老系统全量插入（优先）
        log.info("合并到 tmp_all_coded_land (老系统优先)...");
        int mergedOld = jdbcTemplate.update(
            "INSERT INTO tmp_all_coded_land (src, code, lng, lat, src_id) " +
            "SELECT src, code, lng, lat, src_id FROM tmp_old_coded_dedup"
        );
        log.info("老系统入库: {} 条", mergedOld);

        // 5. v1 仅插入老系统没有的 code
        log.info("合并 v1 补全...");
        int mergedV1 = jdbcTemplate.update(
            "INSERT IGNORE INTO tmp_all_coded_land (src, code, lng, lat, src_id) " +
            "SELECT src, code, lng, lat, src_id FROM tmp_v1_coded_dedup"
        );
        log.info("v1 补全: {} 条", mergedV1);

        // 6. 老系统无编码
        log.info("抽取老系统无编码数据...");
        int oldUncoded = extractOldUncoded();
        log.info("老系统无编码数据: {} 条", oldUncoded);

        // 7. v1 无编码
        log.info("抽取 v1 无编码数据...");
        int v1Uncoded = jdbcTemplate.update(
            "INSERT INTO tmp_v1_uncoded_land (id, lng, lat) " +
            "SELECT id, CAST(longitude AS DECIMAL(18,10)), CAST(latitude AS DECIMAL(18,10)) " +
            "FROM hr_bd_land " +
            "WHERE deleted=0 AND (data_code IS NULL OR data_code='' OR data_code='0') AND longitude IS NOT NULL AND latitude IS NOT NULL AND longitude<>'' AND latitude<>''"
        );
        log.info("v1 无编码数据: {} 条", v1Uncoded);

        // 清理中间表
        jdbcTemplate.execute("DROP TABLE IF EXISTS tmp_old_coded_dedup");
        jdbcTemplate.execute("DROP TABLE IF EXISTS tmp_v1_coded_dedup");
    }

    private int extractOldUncoded() {
        String sql = "INSERT INTO tmp_old_uncoded_land (id, lng, lat) " +
            "SELECT id, " +
            "       CAST(JSON_UNQUOTE(JSON_EXTRACT(land_jwd_gcj02, '$.properties.center.lng')) AS DECIMAL(18,10)), " +
            "       CAST(JSON_UNQUOTE(JSON_EXTRACT(land_jwd_gcj02, '$.properties.center.lat')) AS DECIMAL(18,10)) " +
            "FROM t_farm_land " +
            "WHERE del_flag = 0 " +
            "  AND (platform_code IS NULL OR platform_code = '' OR platform_code = '0') " +
            "  AND land_jwd_gcj02 IS NOT NULL AND land_jwd_gcj02 <> '' " +
            "  AND JSON_EXTRACT(land_jwd_gcj02, '$.properties.center.lng') IS NOT NULL " +
            "  AND JSON_EXTRACT(land_jwd_gcj02, '$.properties.center.lat') IS NOT NULL";
        int count = jdbcTemplate.update(sql);
        if (count > 0) {
            log.info("使用 land_jwd_gcj02 成功抽取无编码 {} 条", count);
            return count;
        }

        log.info("land_jwd_gcj02 无数据，尝试 land_jwd...");
        sql = "INSERT INTO tmp_old_uncoded_land (id, lng, lat) " +
            "SELECT id, " +
            "       CAST(JSON_UNQUOTE(JSON_EXTRACT(land_jwd, '$.properties.center.lng')) AS DECIMAL(18,10)), " +
            "       CAST(JSON_UNQUOTE(JSON_EXTRACT(land_jwd, '$.properties.center.lat')) AS DECIMAL(18,10)) " +
            "FROM t_farm_land " +
            "WHERE del_flag = 0 " +
            "  AND (platform_code IS NULL OR platform_code = '' OR platform_code = '0') " +
            "  AND land_jwd IS NOT NULL AND land_jwd <> '' " +
            "  AND JSON_EXTRACT(land_jwd, '$.properties.center.lng') IS NOT NULL " +
            "  AND JSON_EXTRACT(land_jwd, '$.properties.center.lat') IS NOT NULL";
        count = jdbcTemplate.update(sql);
        log.info("使用 land_jwd 抽取无编码 {} 条", count);
        return count;
    }

    private void verifyStats() {
        log.info("=== 验证统计 ===");
        String sql = "SELECT 'tmp_all_coded_land' AS tbl, COUNT(*) AS cnt FROM tmp_all_coded_land " +
            "UNION ALL SELECT 'tmp_old_uncoded_land', COUNT(*) FROM tmp_old_uncoded_land " +
            "UNION ALL SELECT 'tmp_v1_uncoded_land', COUNT(*) FROM tmp_v1_uncoded_land " +
            "UNION ALL SELECT 't_farm_land总有效', COUNT(*) FROM t_farm_land WHERE del_flag = 0 " +
            "UNION ALL SELECT 'hr_bd_land总有效', COUNT(*) FROM hr_bd_land WHERE deleted = 0";
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);
        for (Map<String, Object> row : results) {
            log.info("{}: {}", row.get("tbl"), row.get("cnt"));
        }

        // 详细校验：有坐标的数据覆盖率
        checkCoverage();
    }

    private void checkCoverage() {
        // v1 覆盖率
        Map<String, Object> v1Total = jdbcTemplate.queryForMap(
            "SELECT COUNT(*) total, " +
            "SUM(CASE WHEN longitude IS NOT NULL AND latitude IS NOT NULL AND longitude<>'' AND latitude<>'' THEN 1 ELSE 0 END) has_coord " +
            "FROM hr_bd_land WHERE deleted = 0"
        );
        log.info("v1: 总有效={}, 有坐标={}, 覆盖率={}%",
            v1Total.get("total"), v1Total.get("has_coord"),
            ((Number)v1Total.get("has_coord")).doubleValue() / ((Number)v1Total.get("total")).doubleValue() * 100
        );

        // 老系统覆盖率
        Map<String, Object> oldTotal = jdbcTemplate.queryForMap(
            "SELECT COUNT(*) total, " +
            "SUM(CASE WHEN land_jwd_gcj02 IS NOT NULL AND land_jwd_gcj02<>'' THEN 1 ELSE 0 END) has_gcj02, " +
            "SUM(CASE WHEN land_jwd IS NOT NULL AND land_jwd<>'' THEN 1 ELSE 0 END) has_jwd " +
            "FROM t_farm_land WHERE del_flag = 0"
        );
        log.info("老系统: 总有效={}, 有gcj02={}, 有jwd={}",
            oldTotal.get("total"), oldTotal.get("has_gcj02"), oldTotal.get("has_jwd"));
    }
}