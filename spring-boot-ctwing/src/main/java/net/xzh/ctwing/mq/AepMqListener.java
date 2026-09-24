package net.xzh.ctwing.mq;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.ctiot.aep.mqmsgpush.sdk.IMsgConsumer;
import com.ctiot.aep.mqmsgpush.sdk.IMsgListener;
import com.ctiot.aep.mqmsgpush.sdk.MqMsgConsumer;

import lombok.RequiredArgsConstructor;

/**
 * ctwing (电信AEP) 北向 MQ 消息推送 —— 接收侧监听（容器启动后自动开始接收）
 *
 * <p>使用官方消息推送消费 SDK {@code com.ctiot.aep:mq-msgpush-sdk}（底层 Apache Pulsar，
 * 非 MQTT）。收到消息后仅做格式化输出到控制台，拿到最原始的上报数据，不做业务转写。</p>
 *
 * <p>与发送侧代码（controller/service）完全分离，独立在 {@code net.xzh.ctwing.mq} 包下，
 * 为将来拆分发送/接收两个工程做准备。</p>
 */
@Order(1)
@Component
@RequiredArgsConstructor
public class AepMqListener implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(AepMqListener.class);

	/** 设备过滤开关：true=只输出 {@link #FILTER_DEVICE_ID} 的日志，false=输出全部设备 */
	private static final boolean DEVICE_FILTER_ENABLED = true;

	/** 只关注的设备ID（硬编码，演示用） */
	private static final String FILTER_DEVICE_ID = "17080670863434085574173";

	private final AepMqProperties aepMqProperties;

	@Override
	public void run(ApplicationArguments args) {
		if (aepMqProperties.getServer() == null || aepMqProperties.getServer().trim().isEmpty()
				|| aepMqProperties.getTopics() == null || aepMqProperties.getTopics().isEmpty()) {
			log.info("===========电信AEP平台MQ监听未启用（aep.mq.server/topics 未配置），跳过==========");
			return;
		}
		log.info("===========电信AEP平台MQ监听初始化开始==========");
		//创建消息接收类
		IMsgConsumer consumer = new MqMsgConsumer();
		try {
			IMsgListener msgListener = this::onMessage;
			//初始化（第4参为客户端/订阅标识，生产验证可传空串用默认值）
			boolean ok = consumer.init(aepMqProperties.getServer(), aepMqProperties.getTenantId(),
					aepMqProperties.getToken(), "", aepMqProperties.getTopics(), msgListener);
			if (ok) {
				//开始接收消息
				consumer.start();
				log.info("===========电信AEP平台MQ监听初始化完成==========");
			} else {
				log.error("电信AEP平台MQ监听初始化失败，请检查 aep.mq.* 配置（server/tenantId/token/topics）");
			}
		} catch (Exception e) {
			log.error("监听AEP平台设备数据上报发生异常,e={}", e.getMessage());
		}
	}

	/**
	 * 收到推送消息：先按硬编码 deviceId 过滤，再按 messageType 输出语义化日志
	 *
	 * @param msg 平台推送的原始消息（JSON 串）
	 */
	private void onMessage(String msg) {
		try {
			JSONObject json = JSON.parseObject(msg);
			String deviceId = json.getString("deviceId");
			if (DEVICE_FILTER_ENABLED && !FILTER_DEVICE_ID.equals(deviceId)) {
				return;
			}
			String messageType = json.getString("messageType");
			if ("commandResponse".equals(messageType)) {
				logCommandResponse(deviceId, json);
			} else if ("dataReport".equals(messageType)) {
				logDataReport(deviceId, json);
			} else if ("deviceOnlineOfflineReport".equals(messageType)) {
				logOnlineOffline(deviceId, json);
			} else {
				log.info("设备:{} 其它消息({}):\n{}", deviceId, messageType,
						json.toJSONString(JSONWriter.Feature.PrettyFormat));
			}
		} catch (Exception e) {
			log.warn("【AEP MQ】消息非JSON格式，仅输出原文: {}", msg);
		}
	}

	/**
	 * 指令回执日志（AEP 平台产生，不经过设备数据域）
	 *
	 * <p>SENT=指令已下发 / DELIVERED=已送达设备 / 其它=失败类回执</p>
	 */
	private void logCommandResponse(String deviceId, JSONObject json) {
		int taskId = json.getIntValue("taskId");
		JSONObject result = json.getJSONObject("result");
		String resultCode = result == null ? "" : result.getString("resultCode");
		String resultDetail = result == null ? "" : result.getString("resultDetail");
		switch (resultCode == null ? "" : resultCode) {
		case "SENT":
			log.info("设备:{} 【1·指令到达平台】taskId={}, resultDetail={}",
					deviceId, taskId, resultDetail);
			break;
		case "DELIVERED":
			log.info("设备:{} 【2·指令送到设备】taskId={}", deviceId, taskId);
			break;
		case "EXPIRED":
			log.info("设备:{} 【链路失败·设备离线过期】taskId={}", deviceId, taskId);
			break;
		case "TIMEOUT":
			log.info("设备:{} 【链路失败·超时未应答】taskId={}", deviceId, taskId);
			break;
		default:
			log.info("设备:{} 【指令回执·其它】resultCode={}, taskId={}, resultDetail={}",
					deviceId, resultCode, taskId, resultDetail);
			break;
		}
	}

	/**
	 * 上/下线报告日志（平台产生，eventType: 1=上线 0=下线，无 APPdata）
	 */
	private void logOnlineOffline(String deviceId, JSONObject json) {
		String eventType = json.getString("eventType");
		if ("1".equals(eventType)) {
			log.info("设备:{} 【0·设备上线】(通常随后上报info版本、para_request要参数)", deviceId);
		} else if ("0".equals(eventType)) {
			log.info("设备:{} 【0·设备下线】(此后再发指令会走EXPIRED过期分支)", deviceId);
		} else {
			log.info("设备:{} 【上下线报告·其它】eventType={}, msg={}", deviceId, eventType,
					json.toJSONString(JSONWriter.Feature.PrettyFormat));
		}
	}

	/**
	 * 数据上报日志：解码 APPdata 帧后，先给一行语义化结论，再打明细
	 *
	 * <p>para_ack=指令执行完毕；其它（meas/status/info/para_request...）=数据上报</p>
	 */
	private void logDataReport(String deviceId, JSONObject json) {
		JSONObject dataJson = decodeAppData(json);
		if (dataJson == null) {
			log.info("设备:{} 【4·设备数据上报·无APPdata】raw={}", deviceId, json);
			return;
		}
		if (dataJson.containsKey("para_ack")) {
			log.info("设备:{} 【3·设备执行成功】para_ack={}", deviceId, dataJson.toJSONString());
			return;
		}
		Object cmdObj = dataJson.get("cmd");
		//cmd为JSON对象 = 设备对getdata等查询的指令响应
		if (cmdObj != null && !(cmdObj instanceof String)) {
			log.info("设备:{} 【4·指令响应·查询结果】data={}", deviceId, dataJson.toJSONString());
			return;
		}
		String cmd = cmdObj == null ? null : cmdObj.toString();
		if (cmd == null) {
			if (dataJson.containsKey("content")) {
				log.info("设备:{} 【5·设备告警】data={}", deviceId, dataJson.toJSONString());
			} else {
				log.info("设备:{} 【4·设备数据上报·未知结构】data={}", deviceId, dataJson.toJSONString());
			}
			return;
		}
		switch (cmd) {
		case "meas":
			log.info("设备:{} 【4·设备数据上报·测量meas】(物理到位看这里) data={}", deviceId,
					dataJson.toJSONString());
			break;
		case "status":
			log.info("设备:{} 【4·设备数据上报·状态status】data={}", deviceId, dataJson.toJSONString());
			break;
		case "info":
			log.info("设备:{} 【0.5·设备上报版本info】(常见于上线后) data={}", deviceId,
					dataJson.toJSONString());
			break;
		case "para_request":
			log.info("设备:{} 【0.5·设备请求参数para_request】(设备要配置,应回para_set) data={}",
					deviceId, dataJson.toJSONString());
			break;
		case "para_report":
			log.info("设备:{} 【4·设备数据上报·参数上报para_report】data={}", deviceId,
					dataJson.toJSONString());
			break;
		default:
			log.info("设备:{} 【4·设备数据上报·cmd={}】data={}", deviceId, cmd, dataJson.toJSONString());
			break;
		}
	}

	/**
	 * 解码 payload.APPdata：Base64 → 讯飞自定义帧 → 数据域（通常为 JSON）
	 *
	 * <p>帧格式：功能码(1B)|设备SN(15B)|包序号(1B)|包标记(1B)|数据长度(2B)|数据域(nB)|校验码(2B)</p>
	 *
	 * @return 数据域 JSON；无 APPdata 或解码失败返回 null
	 */
	private JSONObject decodeAppData(JSONObject json) {
		JSONObject payload = json.getJSONObject("payload");
		if (payload == null) {
			return null;
		}
		String appData = payload.getString("APPdata");
		if (appData == null || appData.isEmpty()) {
			return null;
		}
		try {
			byte[] frame = Base64.getDecoder().decode(appData);
			if (frame.length < 22) {
				log.warn("APPdata解码后长度不足({}B)，跳过帧解析", frame.length);
				return null;
			}
			//帧头: 功能码(1)|SN(15)|包序号(1)|包标记(1)|长度(2) = 20B; 数据域从第20字节起, 末尾2B为CRC
			int length = ((frame[18] & 0xFF) << 8) | (frame[19] & 0xFF);
			//防御：长度字段越界时按剩余字节截取（去掉末尾2B校验）
			length = Math.min(length, frame.length - 22);
			String data = length > 0 ? new String(frame, 20, length, StandardCharsets.UTF_8) : "";
			try {
				return JSON.parseObject(data);
			} catch (Exception ignore) {
				log.info("数据域(非JSON): {}", data);
				return null;
			}
		} catch (Exception e) {
			log.warn("APPdata Base64解码失败: {}", e.getMessage());
			return null;
		}
	}
}

