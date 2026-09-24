package net.xzh.ctwing.mq;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * ctwing (电信AEP) 北向 MQ 消息推送 —— 接收侧配置
 *
 * <p>与发送侧 {@link net.xzh.ctwing.config.AepProperties}（{@code aep.send.*}）完全分离：
 * 发送走 {@code aep.send.*}（appKey/appSecret/products），接收走 {@code aep.mq.*}（消息推送服务地址/租户/订阅）。
 * 为将来拆分发送/接收两个工程做准备。</p>
 *
 * <p><b>值来源</b>：AEP 控制台「消息订阅推送 → 开通 MQ 服务」后给出的 broker 地址、租户ID、token、主题，
 * 反填到 {@code application-prod/dev.yml} 的 {@code aep.mq.*}。</p>
 */
@Data
@Component
@ConfigurationProperties(prefix = "aep.mq")
public class AepMqProperties {

	/** 消息推送服务地址（如 {tenantId}.mq-msgpush.ctwing.cn:16651） */
	private String server;
	/** 租户ID（AEP 租户，控制台给出） */
	private String tenantId;
	/** 身份认证 token（AEP 控制台给出的 JWT 串） */
	private String token;
	/** 订阅主题列表（设备上下线 / 数据上报等推送主题，控制台给出） */
	private List<String> topics = new ArrayList<>();
}
