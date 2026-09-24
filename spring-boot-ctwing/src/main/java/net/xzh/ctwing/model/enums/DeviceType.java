package net.xzh.ctwing.model.enums;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 设备类型数据字典（农情设备）
 *
 * <p>设备类型与协议共同决定该设备对应 AEP 平台的哪个产品：<br>
 * 产品 = 协议 × 物模型，同一设备类型不同协议（4G/NB）是两个产品。</p>
 */
public enum DeviceType {

	BALL_VALVE("球阀"),

	BUTTERFLY_VALVE("蝶阀"),

	FERTIGATION_MACHINE("水肥机"),

	WATER_PUMP("水泵"),

	VIDEO_CAMERA("摄像头"),

	WEATHER_STATION("气象站"),

	SOIL_MONITOR("土壤墒情监测仪"),

	PEST_MONITORING_LAMP("虫情测报灯"),

	SPORE_COLLECTOR("孢子捕捉仪");

	private final String name;

	DeviceType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	/**
	 * 按编码（枚举名，不区分大小写）解析设备类型，未知类型抛出异常
	 */
	public static DeviceType from(String code) {
		if (code == null || code.trim().isEmpty()) {
			throw new IllegalArgumentException("缺少必填参数 deviceType");
		}
		try {
			return valueOf(code.trim().toUpperCase());
		} catch (IllegalArgumentException e) {
			String supported = Arrays.stream(values()).map(Enum::name)
					.collect(Collectors.joining(", "));
			throw new IllegalArgumentException("不支持的设备类型: " + code + "，可选值: " + supported);
		}
	}
}