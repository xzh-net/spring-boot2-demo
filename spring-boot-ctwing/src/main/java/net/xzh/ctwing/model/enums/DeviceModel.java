package net.xzh.ctwing.model.enums;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 球阀设备型号数据字典（L/D/T 等结构型号）
 *
 * <p>与 {@link DeviceType} 分层：DeviceType 是产品/物模型维度（如 BALL_VALVE），
 * DeviceModel 是同一产品下的外形/结构型号；mode/exclusive/angles 等调试参数
 * 不入库，由前端 static/valve-config.json 按 model 维护。</p>
 */
public enum DeviceModel {

	L_VALVE("L型阀门"),

	D_DUAL_HEAD("D型双头"),

	D_INTEGRATED("D型一体"),

	T_VALVE("T型阀门"),

	DUAL_HEAD("双头"),

	TRIPLE_HEAD("三头"),

	QUAD_HEAD("四头"),

	PENTA_SINGLE("五通单"),

	PENTA_DUAL("五通双");

	private final String name;

	DeviceModel(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	/**
	 * 按编码（枚举名，不区分大小写）或中文名解析设备型号
	 */
	public static DeviceModel from(String code) {
		if (code == null || code.trim().isEmpty()) {
			throw new IllegalArgumentException("缺少必填参数 deviceModel");
		}
		String key = code.trim();
		for (DeviceModel model : values()) {
			if (model.name().equalsIgnoreCase(key) || model.name.equals(key)) {
				return model;
			}
		}
		String supported = Arrays.stream(values())
				.map(m -> m.name() + "(" + m.name + ")")
				.collect(Collectors.joining(", "));
		throw new IllegalArgumentException("不支持的设备型号: " + code + "，可选值: " + supported);
	}
}
