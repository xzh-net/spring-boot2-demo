package net.xzh.ctwing.model.dto.result;

import lombok.Data;

/**
 * AEP 平台指令下发结果 / 指令详情（CreateCommand / QueryCommand 返回的 result 对象）
 */
@Data
public class AepCommandResult {

	/** 指令ID */
	private Long commandId;

	/** 指令内容（原始指令） */
	private String command;

	/** 指令状态（如：指令已保存 / 指令已发送 / 指令已完成 / 指令发送失败） */
	private String commandStatus;

	/** 设备ID */
	private String deviceId;

	/** IMEI 号（NB设备） */
	private String imei;

	/** 产品ID */
	private Long productId;

	/** 创建者 */
	private String createBy;

	/** 创建时间（毫秒时间戳） */
	private Long createTime;

	/** 结束时间（毫秒时间戳） */
	private Long finishTime;

	/** 执行结果码 */
	private String resultCode;

	/** 执行结果描述 */
	private String resultMessage;
}