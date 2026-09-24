package net.xzh.ctwing.model.entity;

import lombok.Data;

/**
 * 本地 SQLite 设备（device）：仅基础信息 + 状态
 *
 * <p>球阀 mode / exclusive / angles / valves 等调试参数不入库，
 * 由前端 static/valve-config.json 按 model 维护。</p>
 */
@Data
public class Device {

	private Long id;

	/** 设备编号 SN */
	private String sn;

	/** 设备名称 */
	private String name;

	/** 设备类型 {@link net.xzh.ctwing.model.enums.DeviceType} 编码，如 BALL_VALVE */
	private String type;

	/** 设备型号 {@link net.xzh.ctwing.model.enums.DeviceModel} 编码，如 L_VALVE */
	private String model;

	/** 通信方式：4G / NB */
	private String proto;

	/** AEP deviceId（注册后回写） */
	private String deviceId;

	/** 注册状态 0=未注册 1=已注册 */
	private Integer regStatus;
}
