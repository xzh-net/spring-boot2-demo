-- ============================================================
-- V1系统专用：按 id 更新 hr_bd_land 的 formatted_address 和 towncode
-- 从 tmp_v1_uncoded_land 同步
-- 执行前请确认目标表已增加 formatted_address 和 towncode 字段
-- ============================================================

UPDATE hr_bd_land t
INNER JOIN tmp_v1_uncoded_land tmp
    ON tmp.id = t.id
SET t.formatted_address = tmp.formatted_address,
    t.towncode          = tmp.towncode
WHERE t.deleted = 0;
