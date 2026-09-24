package net.xzh.ctwing.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import net.xzh.ctwing.config.AepProperties;
import net.xzh.ctwing.config.AepProperties.ProductConfig;
import net.xzh.ctwing.model.enums.DeviceType;

/**
 * 业务层产品解析器：根据设备类型 + 协议类型 解析 AEP 产品(productId/masterKey)
 *
 * <p>集成层 service 只接收显式的 productId/masterKey；"哪种设备用哪个产品"的
 * 业务判断统一收敛在本类，前端不接触任何平台产品信息。</p>
 */
@Service
@RequiredArgsConstructor
public class AepProductResolver {

	private final AepProperties aepProperties;

	/**
	 * 解析产品信息
	 *
	 * @param type     设备类型，见 {@link DeviceType}
	 * @param protocol 协议类型：4G / NB
	 */
	public ProductInfo resolve(DeviceType type, String protocol) {
		String pt = protocol == null ? null : protocol.trim().toUpperCase();
		if (!"4G".equals(pt) && !"NB".equals(pt)) {
			throw new IllegalArgumentException(
					"不支持的协议类型: " + protocol + "，仅支持 4G / NB");
		}
		for (ProductConfig config : aepProperties.getProducts()) {
			if (config.getType() != null && config.getType().equalsIgnoreCase(type.name())
					&& pt.equals(config.getProtocol() == null ? null : config.getProtocol().trim().toUpperCase())) {
				if (config.getProductId() == null || config.getProductId().trim().isEmpty()
						|| config.getMasterKey() == null
						|| config.getMasterKey().trim().isEmpty()) {
					throw new IllegalArgumentException(
							"未配置 设备类型[" + type.getName() + "] 协议[" + protocol + "] 的产品ID/MasterKey，"
									+ "请在 application-prod.yml / application-dev.yml 的 aep.send.products 中配置");
				}
				return new ProductInfo(config.getProductId(), config.getMasterKey());
			}
		}
		throw new IllegalArgumentException(
				"未配置 设备类型[" + type.getName() + "] 协议[" + protocol + "] 对应的产品，"
						+ "请在 application-prod.yml / application-dev.yml 的 aep.send.products 中配置");
	}

	/**
	 * AEP 产品信息
	 */
	public static class ProductInfo {
		private final String productId;
		private final String masterKey;

		public ProductInfo(String productId, String masterKey) {
			this.productId = productId;
			this.masterKey = masterKey;
		}

		public String getProductId() {
			return productId;
		}

		public String getMasterKey() {
			return masterKey;
		}
	}
}