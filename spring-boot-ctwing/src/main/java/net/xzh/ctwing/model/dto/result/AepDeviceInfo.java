package net.xzh.ctwing.model.dto.result;

import lombok.Data;

/**
 * AEP 平台设备信息（查询列表项 / 查询详情返回）
 */
@Data
public class AepDeviceInfo {

	/** 设备ID */
	private String deviceId;

	/** 设备名称 */
	private String deviceName;

	/** 设备编号（4G设备） */
	private String deviceSn;

	/** IMEI（NB/LWM2M设备） */
	private String imei;

	/** IMSI（NB/LWM2M设备，选填） */
	private String imsi;

	/** 租户ID */
	private String tenantId;

	/** 产品ID */
	private Long productId;

	/** 固件版本 */
	private String firmwareVersion;

	/** 设备状态：0已注册 1已激活 2已注销 */
	private Integer deviceStatus;

	/** 是否自动订阅：0订阅 1不订阅 */
	private Integer autoObserver;

	/** 设备在线状态：1在线 2不在线 */
	private Integer netStatus;

	/** 产品协议类型 */
	private Integer productProtocol;

	/** 创建时间（毫秒时间戳） */
	private Long createTime;

	/** 创建者 */
	private String createBy;

	/** 更新时间（毫秒时间戳） */
	private Long updateTime;

	/** 更新者 */
	private String updateBy;

	/** 激活时间（毫秒时间戳） */
	private Long activeTime;

	/** 注销时间（毫秒时间戳） */
	private Long logoutTime;

	/** 最后上线时间（毫秒时间戳） */
	private Long onlineAt;

	/** 最后下线时间（毫秒时间戳） */
	private Long offlineAt;
}