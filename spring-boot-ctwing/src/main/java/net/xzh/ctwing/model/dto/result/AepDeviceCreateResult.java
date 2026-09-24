package net.xzh.ctwing.model.dto.result;

import lombok.Data;

/**
 * AEP 平台创建设备结果
 *
 * <p>字段对应 AEP CreateDevice 返回的 result 对象。</p>
 */
@Data
public class AepDeviceCreateResult {

	/** 设备ID */
	private String deviceId;

	/** 设备名称 */
	private String deviceName;

	/** 租户ID */
	private String tenantId;

	/** 产品ID */
	private Long productId;

	/** IMEI 号（NB设备） */
	private String imei;

	/** 设备编号（4G设备） */
	private String deviceSn;

	/** NB设备连接token，后续可用于设备接入 */
	private String token;
}