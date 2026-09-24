package net.xzh.ctwing.model.dto.param;

import lombok.Data;

/**
 * AEP CreateCommand 接口请求体参数（统一合并指令下发）
 */
@Data
public class AepCommandParamDTO {

	/** 产品ID，必填 */
	private Long productId;

	/** 设备ID，设备级指令必填 */
	private String deviceId;

	/** 操作者，必填 */
	private String operator;

	/** 指令缓存时长（秒），选填 */
	private Integer ttl;

	/** 指令内容，必填 */
	private Content content;

	@Data
	public static class Content {

		/** 数据类型：1字符串 2十六进制 */
		private Integer dataType;

		/** 指令内容，十六进制透传时填十六进制字符串 */
		private String payload;
	}
}