package net.xzh.ctwing.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * 发送侧：ctwing (电信AEP) 平台配置，配置前缀 aep.send
 */
@Data
@Component
@ConfigurationProperties(prefix = "aep.send")
public class AepProperties {
	/** 应用AppKey */
	private String appKey;
	/** 应用AppSecret */
	private String appSecret;
	/** 操作者ID */
	private String operator;
	/** 指令有效期（秒） */
	private Integer ttl;
	/**
	 * 设备类型 × 协议 → 产品 映射表（deviceType、protocol 均不区分大小写）
	 * <p>产品 = 协议 × 物模型：同类型不同协议的设备是两个产品。</p>
	 */
	private List<ProductConfig> products = new ArrayList<>();

	/**
	 * 产品映射配置项
	 */
	@Data
	public static class ProductConfig {
		/** 设备类型（对应 {@link net.xzh.ctwing.model.enums.DeviceType}，不区分大小写） */
		private String type;
		/** 协议类型（4G / NB，不区分大小写） */
		private String protocol;
		/** AEP 产品ID */
		private String productId;
		/** AEP 产品 MasterKey */
		private String masterKey;
	}
}