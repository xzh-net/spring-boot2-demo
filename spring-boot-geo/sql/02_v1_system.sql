-- ============================================================
-- V1系统 SQL（在V1系统数据库执行）
-- 目标表：hr_bd_land
-- 数据源：tmp_all_coded_land(src='hr_bd_land') + tmp_v1_uncoded_land
-- ============================================================

-- ==================== 1. 备份 ====================

-- 1.1 添加 division_id_bak 字段
SET @col_exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'hr_bd_land' AND COLUMN_NAME = 'division_id_bak'
);
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE hr_bd_land ADD COLUMN division_id_bak VARCHAR(100)',
    'SELECT "division_id_bak already exists"'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 1.2 备份原值
UPDATE hr_bd_land SET division_id_bak = division_id;

-- 1.3 创建全表备份表
SET @bak_table = CONCAT('hr_bd_land_bak_', DATE_FORMAT(NOW(), '%Y%m%d_%H%i%s'));
SET @sql = CONCAT('CREATE TABLE ', @bak_table, ' AS SELECT * FROM hr_bd_land');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;


-- ==================== 2. 更新有编码的数据（tmp_all_coded_land） ====================

-- 2.1 更新 addr_detail
UPDATE hr_bd_land t
INNER JOIN tmp_all_coded_land tmp
    ON tmp.code = t.data_code
SET t.addr_detail = tmp.formatted_address
WHERE t.deleted = 0;

-- 2.2 条件更新 division_id
-- 规则：原值12位且前9位与towncode前9位一致则不更新，否则全部更新为LEFT(towncode,9)
UPDATE hr_bd_land t
INNER JOIN tmp_all_coded_land tmp
    ON tmp.code = t.data_code
SET t.division_id = LEFT(tmp.towncode, 9)
WHERE t.deleted = 0
  AND (
      t.division_id IS NULL
      OR t.division_id = ''
      OR LENGTH(t.division_id) <> 12
      OR LEFT(t.division_id, 9) <> LEFT(tmp.towncode, 9)
  );


-- ==================== 3. 更新无编码的数据（tmp_v1_uncoded_land） ====================

-- 3.1 更新 addr_detail
UPDATE hr_bd_land t
INNER JOIN tmp_v1_uncoded_land tmp
    ON tmp.id = t.id
SET t.addr_detail = tmp.formatted_address
WHERE t.deleted = 0;

-- 3.2 条件更新 division_id
UPDATE hr_bd_land t
INNER JOIN tmp_v1_uncoded_land tmp
    ON tmp.id = t.id
SET t.division_id = LEFT(tmp.towncode, 9)
WHERE t.deleted = 0
  AND (
      t.division_id IS NULL
      OR t.division_id = ''
      OR LENGTH(t.division_id) <> 12
      OR LEFT(t.division_id, 9) <> LEFT(tmp.towncode, 9)
  );


-- ============================================================
-- 校验查询（V1系统）
-- ============================================================

-- 1. 有编码数据统计（tmp_all_coded_land, src='hr_bd_land'）
SELECT
    '有编码数据' AS 数据来源,
    COUNT(*) AS 总记录数,
    SUM(CASE WHEN division_id_bak IS NULL OR division_id_bak = '' THEN 1 ELSE 0 END) AS 原值为空_已更新,
    SUM(CASE WHEN division_id_bak IS NOT NULL AND division_id_bak <> '' AND LENGTH(division_id_bak) <> 12 THEN 1 ELSE 0 END) AS 原值非12位_已更新,
    SUM(CASE WHEN LENGTH(division_id_bak) = 12 AND LEFT(division_id_bak, 9) <> LEFT(tmp.towncode, 9) THEN 1 ELSE 0 END) AS 前9位不匹配_已更新,
    SUM(CASE WHEN LENGTH(division_id_bak) = 12 AND LEFT(division_id_bak, 9) = LEFT(tmp.towncode, 9) THEN 1 ELSE 0 END) AS 前9位匹配_未更新
FROM hr_bd_land
INNER JOIN tmp_all_coded_land tmp ON tmp.code = hr_bd_land.data_code 
WHERE hr_bd_land.deleted = 0 AND hr_bd_land.division_id_bak IS NOT NULL;

-- 2. 无编码数据统计（tmp_v1_uncoded_land）
SELECT
    '无编码数据' AS 数据来源,
    COUNT(*) AS 总记录数,
    SUM(CASE WHEN division_id_bak IS NULL OR division_id_bak = '' THEN 1 ELSE 0 END) AS 原值为空_已更新,
    SUM(CASE WHEN division_id_bak IS NOT NULL AND division_id_bak <> '' AND LENGTH(division_id_bak) <> 12 THEN 1 ELSE 0 END) AS 原值非12位_已更新,
    SUM(CASE WHEN LENGTH(division_id_bak) = 12 AND LEFT(division_id_bak, 9) <> LEFT(tmp.towncode, 9) THEN 1 ELSE 0 END) AS 前9位不匹配_已更新,
    SUM(CASE WHEN LENGTH(division_id_bak) = 12 AND LEFT(division_id_bak, 9) = LEFT(tmp.towncode, 9) THEN 1 ELSE 0 END) AS 前9位匹配_未更新
FROM hr_bd_land
INNER JOIN tmp_v1_uncoded_land tmp ON tmp.id = hr_bd_land.id
WHERE hr_bd_land.deleted = 0 AND hr_bd_land.division_id_bak IS NOT NULL;

-- 3. 抽样：有编码数据中前9位不匹配被更新的记录
SELECT id, data_code, division_id_bak AS 更新前, division_id AS 更新后, LEFT(tmp.towncode, 9) AS towncode前9位
FROM hr_bd_land t
INNER JOIN tmp_all_coded_land tmp ON tmp.code = t.data_code 
WHERE t.deleted = 0
  AND LENGTH(t.division_id_bak) = 12
  AND LEFT(t.division_id_bak, 9) <> LEFT(tmp.towncode, 9)
LIMIT 10;

-- 4. 抽样：有编码数据中未更新的记录（原值前9位匹配）
SELECT id, data_code, division_id_bak AS 原值, division_id AS 当前值, LEFT(tmp.towncode, 9) AS towncode前9位
FROM hr_bd_land t
INNER JOIN tmp_all_coded_land tmp ON tmp.code = t.data_code 
WHERE t.deleted = 0
  AND LENGTH(t.division_id_bak) = 12
  AND LEFT(t.division_id_bak, 9) = LEFT(tmp.towncode, 9)
LIMIT 10;

-- 5. 抽样：无编码数据中前9位不匹配被更新的记录
SELECT t.id, t.data_code, t.division_id_bak AS 更新前, t.division_id AS 更新后, LEFT(tmp.towncode, 9) AS towncode前9位
FROM hr_bd_land t
INNER JOIN tmp_v1_uncoded_land tmp ON tmp.id = t.id
WHERE t.deleted = 0
  AND LENGTH(t.division_id_bak) = 12
  AND LEFT(t.division_id_bak, 9) <> LEFT(tmp.towncode, 9)
LIMIT 10;

-- 6. 抽样：无编码数据中未更新的记录（原值前9位匹配）
SELECT t.id, t.data_code, t.division_id_bak AS 原值, t.division_id AS 当前值, LEFT(tmp.towncode, 9) AS towncode前9位
FROM hr_bd_land t
INNER JOIN tmp_v1_uncoded_land tmp ON tmp.id = t.id
WHERE t.deleted = 0
  AND LENGTH(t.division_id_bak) = 12
  AND LEFT(t.division_id_bak, 9) = LEFT(tmp.towncode, 9)
LIMIT 10;

-- 7. 检查 addr_detail 是否全部更新成功
SELECT '有编码数据' AS 数据来源, COUNT(*) AS 未更新地址的数量
FROM hr_bd_land t
INNER JOIN tmp_all_coded_land tmp ON tmp.code = t.data_code 
WHERE t.deleted = 0 AND (t.addr_detail IS NULL OR t.addr_detail = '')
UNION ALL
SELECT '无编码数据', COUNT(*)
FROM hr_bd_land t
INNER JOIN tmp_v1_uncoded_land tmp ON tmp.id = t.id
WHERE t.deleted = 0 AND (t.addr_detail IS NULL OR t.addr_detail = '');

select division_id,division_id_bak,a.* from hr_bd_land a where deleted = 0 and length(division_id_bak) = 12 order by division_id