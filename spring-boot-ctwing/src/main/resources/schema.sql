-- 本地设备表：仅基础信息 + 状态（球阀 mode/exclusive/angles 等参数在 static/valve-config.json）
CREATE TABLE IF NOT EXISTS device (
  id           INTEGER PRIMARY KEY AUTOINCREMENT,
  sn           VARCHAR(32)  NOT NULL UNIQUE,   -- 设备编号
  name         VARCHAR(64),                    -- 设备名称
  type         VARCHAR(32)  NOT NULL,          -- 设备类型 DeviceType 编码，如 BALL_VALVE
  model        VARCHAR(32)  NOT NULL,          -- 设备型号 DeviceModel 编码，如 L_VALVE
  proto        VARCHAR(8)   NOT NULL,          -- 通信方式 4G / NB
  device_id    VARCHAR(64),                    -- AEP deviceId（注册后回写）
  reg_status   INTEGER      NOT NULL DEFAULT 0, -- 注册状态 0=未注册 1=已注册
  create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

