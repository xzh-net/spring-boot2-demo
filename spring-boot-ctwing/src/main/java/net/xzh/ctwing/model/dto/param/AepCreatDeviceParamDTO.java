package net.xzh.ctwing.model.dto.param;

import lombok.Data;

/**
 * AEP CreateDevice 接口请求体参数
 *
 * <p>对应 AEP "增加设备" 接口文档，LWM2M/NB 协议必填 imei 与 other。</p>
 */
@Data
public class AepCreatDeviceParamDTO {

	/** 设备名称，必填 */
	private String deviceName;

	/** 设备编号，MQTT/T-Link/TCP/HTTP/JT808等协议必填 */
	private String deviceSn;

	/** IMEI，LWM2M/NB网关协议必填 */
	private String imei;

	/** 操作者，必填 */
	private String operator;

	/** 产品ID，必填 */
	private Long productId;

	/** LWM2M/NB协议必填参数 */
	private Other other;

	@Data
	public static class Other {

		/** 是否订阅：0自动订阅 1取消自动订阅 */
		private Integer autoObserver;

		/** IMSI，总长度不超过15位，选填 */
		private String imsi;

		/** PSK值，16位字符串，选填 */
		private String pskValue;

		/** PSK类型，0普通字符串 1十六进制字符串，选填 */
		private Integer pskType;
	}
}