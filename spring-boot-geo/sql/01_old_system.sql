-- ============================================================
-- 老系统 SQL（在老系统数据库执行）
-- 目标表：t_farm_land
-- 数据源：tmp_all_coded_land(src='t_farm_land') + tmp_old_uncoded_land
-- ============================================================

-- ==================== 1. 备份 ====================

-- 1.1 添加 district_bak 字段
SET @col_exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 't_farm_land' AND COLUMN_NAME = 'district_bak'
);
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE t_farm_land ADD COLUMN district_bak VARCHAR(100)',
    'SELECT "district_bak already exists"'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 1.2 备份原值
UPDATE t_farm_land SET district_bak = district;

-- 1.3 创建全表备份表
SET @bak_table = CONCAT('t_farm_land_bak_', DATE_FORMAT(NOW(), '%Y%m%d_%H%i%s'));
SET @sql = CONCAT('CREATE TABLE ', @bak_table, ' AS SELECT * FROM t_farm_land');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;


-- ==================== 2. 更新有编码的数据（tmp_all_coded_land） ====================

-- 2.1 更新 detailed_address
UPDATE t_farm_land t
INNER JOIN tmp_all_coded_land tmp
    ON tmp.code = t.platform_code
SET t.detailed_address = tmp.formatted_address
WHERE t.del_flag = 0;

-- 2.2 条件更新 district
-- 规则：原值12位且前9位与towncode前9位一致则不更新，否则全部更新为LEFT(towncode,9)
UPDATE t_farm_land t
INNER JOIN tmp_all_coded_land tmp
    ON tmp.code = t.platform_code
SET t.district = LEFT(tmp.towncode, 9)
WHERE t.del_flag = 0
  AND (
      t.district IS NULL
      OR t.district = ''
      OR LENGTH(t.district) <> 12
      OR LEFT(t.district, 9) <> LEFT(tmp.towncode, 9)
  );


-- ==================== 3. 更新无编码的数据（tmp_old_uncoded_land） ====================

-- 3.1 更新 detailed_address
UPDATE t_farm_land t
INNER JOIN tmp_old_uncoded_land tmp
    ON tmp.id = t.id
SET t.detailed_address = tmp.formatted_address
WHERE t.del_flag = 0;

-- 3.2 条件更新 district
UPDATE t_farm_land t
INNER JOIN tmp_old_uncoded_land tmp
    ON tmp.id = t.id
SET t.district = LEFT(tmp.towncode, 9)
WHERE t.del_flag = 0
  AND (
      t.district IS NULL
      OR t.district = ''
      OR LENGTH(t.district) <> 12
      OR LEFT(t.district, 9) <> LEFT(tmp.towncode, 9)
  );


-- ============================================================
-- 校验查询（老系统）
-- ============================================================

-- 1. 有编码数据统计（tmp_all_coded_land, src='t_farm_land'）
SELECT
    '有编码数据' AS 数据来源,
    COUNT(*) AS 总记录数,
    SUM(CASE WHEN district_bak IS NULL OR district_bak = '' THEN 1 ELSE 0 END) AS 原值为空_已更新,
    SUM(CASE WHEN district_bak IS NOT NULL AND district_bak <> '' AND LENGTH(district_bak) <> 12 THEN 1 ELSE 0 END) AS 原值非12位_已更新,
    SUM(CASE WHEN LENGTH(district_bak) = 12 AND LEFT(district_bak, 9) <> LEFT(tmp.towncode, 9) THEN 1 ELSE 0 END) AS 前9位不匹配_已更新,
    SUM(CASE WHEN LENGTH(district_bak) = 12 AND LEFT(district_bak, 9) = LEFT(tmp.towncode, 9) THEN 1 ELSE 0 END) AS 前9位匹配_未更新
FROM t_farm_land
INNER JOIN tmp_all_coded_land tmp ON tmp.code = t_farm_land.platform_code 
WHERE t_farm_land.del_flag = 0 AND t_farm_land.district_bak IS NOT NULL;

-- 2. 无编码数据统计（tmp_old_uncoded_land）
SELECT
    '无编码数据' AS 数据来源,
    COUNT(*) AS 总记录数,
    SUM(CASE WHEN district_bak IS NULL OR district_bak = '' THEN 1 ELSE 0 END) AS 原值为空_已更新,
    SUM(CASE WHEN district_bak IS NOT NULL AND district_bak <> '' AND LENGTH(district_bak) <> 12 THEN 1 ELSE 0 END) AS 原值非12位_已更新,
    SUM(CASE WHEN LENGTH(district_bak) = 12 AND LEFT(district_bak, 9) <> LEFT(tmp.towncode, 9) THEN 1 ELSE 0 END) AS 前9位不匹配_已更新,
    SUM(CASE WHEN LENGTH(district_bak) = 12 AND LEFT(district_bak, 9) = LEFT(tmp.towncode, 9) THEN 1 ELSE 0 END) AS 前9位匹配_未更新
FROM t_farm_land
INNER JOIN tmp_old_uncoded_land tmp ON tmp.id = t_farm_land.id
WHERE t_farm_land.del_flag = 0 AND t_farm_land.district_bak IS NOT NULL;

-- 3. 抽样：有编码数据中前9位不匹配被更新的记录
SELECT id, platform_code, district_bak AS 更新前, district AS 更新后, LEFT(tmp.towncode, 9) AS towncode前9位
FROM t_farm_land t
INNER JOIN tmp_all_coded_land tmp ON tmp.code = t.platform_code 
WHERE t.del_flag = 0
  AND LENGTH(t.district_bak) = 12
  AND LEFT(t.district_bak, 9) <> LEFT(tmp.towncode, 9)
LIMIT 10;

-- 4. 抽样：有编码数据中未更新的记录（原值前9位匹配）
SELECT id, platform_code, district_bak AS 原值, district AS 当前值, LEFT(tmp.towncode, 9) AS towncode前9位
FROM t_farm_land t
INNER JOIN tmp_all_coded_land tmp ON tmp.code = t.platform_code 
WHERE t.del_flag = 0
  AND LENGTH(t.district_bak) = 12
  AND LEFT(t.district_bak, 9) = LEFT(tmp.towncode, 9)
LIMIT 10;

-- 5. 抽样：无编码数据中前9位不匹配被更新的记录
SELECT t.id, t.platform_code, t.district_bak AS 更新前, t.district AS 更新后, LEFT(tmp.towncode, 9) AS towncode前9位
FROM t_farm_land t
INNER JOIN tmp_old_uncoded_land tmp ON tmp.id = t.id
WHERE t.del_flag = 0
  AND LENGTH(t.district_bak) = 12
  AND LEFT(t.district_bak, 9) <> LEFT(tmp.towncode, 9)
LIMIT 10;

-- 6. 抽样：无编码数据中未更新的记录（原值前9位匹配）
SELECT t.id, t.platform_code, t.district_bak AS 原值, t.district AS 当前值, LEFT(tmp.towncode, 9) AS towncode前9位
FROM t_farm_land t
INNER JOIN tmp_old_uncoded_land tmp ON tmp.id = t.id
WHERE t.del_flag = 0
  AND LENGTH(t.district_bak) = 12
  AND LEFT(t.district_bak, 9) = LEFT(tmp.towncode, 9)
LIMIT 10;

-- 7. 检查 detailed_address 是否全部更新成功
SELECT '有编码数据' AS 数据来源, COUNT(*) AS 未更新地址的数量
FROM t_farm_land t
INNER JOIN tmp_all_coded_land tmp ON tmp.code = t.platform_code 
WHERE t.del_flag = 0 AND (t.detailed_address IS NULL OR t.detailed_address = '')
UNION ALL
SELECT '无编码数据', COUNT(*)
FROM t_farm_land t
INNER JOIN tmp_old_uncoded_land tmp ON tmp.id = t.id
WHERE t.del_flag = 0 AND (t.detailed_address IS NULL OR t.detailed_address = '');


select district,district_bak,a.* from t_farm_land a where a.del_flag = 0  and length(district_bak) = 12 order by district;