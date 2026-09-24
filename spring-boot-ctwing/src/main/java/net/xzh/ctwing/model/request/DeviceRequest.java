package net.xzh.ctwing.model.request;

import lombok.Data;

/**
 * 本地设备新增/修改请求（SQLite device，仅基础信息）
 */
@Data
public class DeviceRequest {

	/** 设备编号 SN（必填、唯一） */
	private String sn;

	/** 设备名称 */
	private String name;

	/** 设备类型 DeviceType 编码，如 BALL_VALVE（必填） */
	private String type;

	/** 设备型号 DeviceModel 编码，如 L_VALVE（必填） */
	private String model;

	/** 通信方式 4G/NB（必填） */
	private String proto;

	/** AEP deviceId（可空，注册后回写） */
	private String deviceId;
}
