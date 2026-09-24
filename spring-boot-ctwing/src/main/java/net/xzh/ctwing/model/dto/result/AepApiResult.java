package net.xzh.ctwing.model.dto.result;

import lombok.Data;

/**
 * AEP 平台接口统一响应体
 *
 * @param <T> result 数据类型
 */
@Data
public class AepApiResult<T> {

	/** 平台状态码，0 表示成功 */
	private Integer code;

	/** 平台返回描述 */
	private String msg;

	/** 业务数据 */
	private T result;
}