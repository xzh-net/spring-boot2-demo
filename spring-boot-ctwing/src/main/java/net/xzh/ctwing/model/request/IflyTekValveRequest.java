package net.xzh.ctwing.model.request;

import java.util.List;

import lombok.Data;

/**
 * 讯飞电动球阀控制请求（慧种田 App 同款报文）
 *
 * <p>单阀传 dir + open；多头阀（双头/三头/四头）传 openlist；
 * 产品由服务端按「设备类型 + 协议类型」解析，deviceId 为空时 4G 自动按 productId+sn 拼接。</p>
 */
@Data
public class IflyTekValveRequest {

	/** 设备类型，默认球阀 */
	private String deviceType = "BALL_VALVE";

	/** 协议类型（必填）：4G / NB */
	private String protocolType;

	/** AEP设备ID（4G 可不传，服务端按 productId+sn 拼接；NB 必传平台GUID） */
	private String deviceId;

	/** 设备编号 SN（必填，15位） */
	private String sn;

	/** 动作方向（单阀）：0关闭 1=A侧 2=B侧 3=AB全开 */
	private Integer dir;

	/** 开启角度 0-100（单阀） */
	private Integer open;

	/** 各头开度列表（多头阀，替代 dir/open） */
	private List<Integer> openlist;

	/** 指令缓存时长（秒，选填，默认取配置 ttl） */
	private Integer ttl;
}
