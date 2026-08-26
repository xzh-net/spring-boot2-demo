-- ============================================================
-- 按 code 更新目标表的 formatted_address 和 towncode
-- 从 tmp_all_coded_land 同步到 t_farm_land 和 hr_bd_land
-- 执行前请确认目标表已增加 formatted_address 和 towncode 字段
-- ============================================================

-- 1. 更新 t_farm_land（老系统，code = platform_code）
UPDATE t_farm_land t
INNER JOIN tmp_all_coded_land tmp
    ON tmp.code = t.platform_code
   AND tmp.src = 't_farm_land'
SET t.formatted_address = tmp.formatted_address,
    t.towncode          = tmp.towncode
WHERE t.del_flag = 0;

-- 2. 更新 hr_bd_land（V1系统，code = data_code）
UPDATE hr_bd_land t
INNER JOIN tmp_all_coded_land tmp
    ON tmp.code = t.data_code
   AND tmp.src = 'hr_bd_land'
SET t.formatted_address = tmp.formatted_address,
    t.towncode          = tmp.towncode
WHERE t.deleted = 0;
