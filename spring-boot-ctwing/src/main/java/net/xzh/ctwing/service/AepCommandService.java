package net.xzh.ctwing.service;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ctg.ag.sdk.biz.AepDeviceCommandClient;
import com.ctg.ag.sdk.biz.aep_device_command.CreateCommandRequest;
import com.ctg.ag.sdk.core.model.BaseApiResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.xzh.ctwing.config.AepProperties;
import net.xzh.ctwing.model.dto.param.AepCommandParamDTO;
import net.xzh.ctwing.model.dto.result.AepCommandResult;
import net.xzh.ctwing.model.enums.DeviceType;
import net.xzh.ctwing.model.request.IflyTekValveRequest;
import net.xzh.ctwing.service.AepProductResolver.ProductInfo;
import net.xzh.ctwing.util.IflyTekValvePayloadUtil;

/**
 * ctwing (电信AEP) 平台设备控制服务（指令下发 / 讯飞球阀全流程）
 *
 * <p>脱离 Controller 可独立调用：组帧、产品解析、deviceId 解析、AEP 下发均在本类完成。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AepCommandService {

	/** 指令数据类型：十六进制 */
	private static final int DATA_TYPE_HEX = 2;

	private final AepProperties aepProperties;
	private final AepProductResolver productResolver;

	private AepDeviceCommandClient commandClient;

	/**
	 * 讯飞球阀：组帧 → 解析产品/deviceId → 打印业务明文 → AEP 下发
	 *
	 * <p>返回：commandId / deviceId / taskNo / json / hex</p>
	 */
	public Map<String, Object> sendIflyTekValve(IflyTekValveRequest request) {
		Map<String, String> frame = buildIflyTekFrame(request);
		ProductInfo product = productResolver.resolve(
				DeviceType.from(request.getDeviceType()), request.getProtocolType());
		String deviceId = resolveIflyTekDeviceId(request, product);
		String payload = frame.get("hex");

		// 下发前置：业务明文参数（组帧意图，非 hex）
		log.info("设备:{} 【下发·业务参数】sn={}, taskNo={}, dir={}, open={}, openlist={}, json={}",
				deviceId, request.getSn(), frame.get("taskNo"), request.getDir(),
				request.getOpen(), request.getOpenlist(), frame.get("json"));

		AepCommandResult result = sendCommand(product.getProductId(), product.getMasterKey(),
				deviceId, payload, request.getTtl());

		Map<String, Object> data = new LinkedHashMap<>();
		data.put("commandId", result == null ? null : result.getCommandId());
		data.put("deviceId", deviceId);
		data.put("taskNo", frame.get("taskNo"));
		data.put("json", frame.get("json"));
		data.put("hex", payload);
		return data;
	}

	/**
	 * 构造讯飞帧：taskNo + 数据域 JSON + 完整帧 hex
	 */
	public Map<String, String> buildIflyTekFrame(IflyTekValveRequest request) {
		String sn = request.getSn();
		if (sn == null || sn.trim().isEmpty()) {
			throw new IllegalArgumentException("缺失必填参数 sn");
		}
		if (sn.length() > 15) {
			throw new IllegalArgumentException("sn 不能超过15个字符: " + sn);
		}
		String taskNo = String.format("%08d", new Random().nextInt(100_000_000));
		String json;
		List<Integer> openlist = request.getOpenlist();
		if (openlist != null && !openlist.isEmpty()) {
			json = IflyTekValvePayloadUtil.buildParaSetJson(taskNo, openlist);
		} else {
			if (request.getDir() == null || request.getOpen() == null) {
				throw new IllegalArgumentException(
						"单阀需要 dir(0-3) + open(0-100)，多头阀请传 openlist");
			}
			if (request.getDir() < 0 || request.getDir() > 3) {
				throw new IllegalArgumentException("dir 取值范围 0-3: " + request.getDir());
			}
			if (request.getOpen() < 0 || request.getOpen() > 100) {
				throw new IllegalArgumentException("open 取值范围 0-100: " + request.getOpen());
			}
			json = IflyTekValvePayloadUtil.buildParaSetJson(taskNo, request.getDir(),
					request.getOpen());
		}
		Map<String, String> frame = new LinkedHashMap<>();
		frame.put("taskNo", taskNo);
		frame.put("json", json);
		frame.put("hex", IflyTekValvePayloadUtil.wrapFrame(sn.trim(), json));
		return frame;
	}

	/**
	 * 解析 deviceId：优先用请求里的；4G 缺省按 productId+sn 拼接；NB 必须显式传
	 */
	public String resolveIflyTekDeviceId(IflyTekValveRequest request, ProductInfo product) {
		if (request.getDeviceId() != null && !request.getDeviceId().trim().isEmpty()) {
			return request.getDeviceId().trim();
		}
		String protocol = request.getProtocolType() == null ? "" : request.getProtocolType().trim();
		if ("NB".equalsIgnoreCase(protocol)) {
			throw new IllegalArgumentException(
					"NB(LWM2M)设备 deviceId 为平台GUID，请从AEP控制台获取后传入");
		}
		return product.getProductId() + request.getSn().trim();
	}

	/**
	 * 下发控制指令（十六进制透传报文）
	 *
	 * @param productId 产品ID
	 * @param masterKey 产品MasterKey
	 * @param deviceId  AEP设备ID
	 * @param payload   十六进制指令报文
	 * @param ttl       指令缓存时长（秒），为空取配置默认值
	 */
	public AepCommandResult sendCommand(String productId, String masterKey, String deviceId,
			String payload, Integer ttl) {
		if (payload == null || payload.trim().isEmpty()) {
			throw new IllegalArgumentException("指令报文 payload 不能为空");
		}

		AepCommandParamDTO paramDTO = new AepCommandParamDTO();
		paramDTO.setProductId(Long.valueOf(requireProductId(productId)));
		paramDTO.setDeviceId(deviceId);
		paramDTO.setOperator(aepProperties.getOperator());
		paramDTO.setTtl(ttl == null ? aepProperties.getTtl() : ttl);

		AepCommandParamDTO.Content content = new AepCommandParamDTO.Content();
		content.setDataType(DATA_TYPE_HEX);
		content.setPayload(payload);
		paramDTO.setContent(content);

		CreateCommandRequest request = new CreateCommandRequest();
		request.setParamMasterKey(requireMasterKey(masterKey));
		request.setBody(JSON.toJSONBytes(paramDTO));

		// ① 下发：参数明文
		log.info("设备:{} 【下发·指令参数】productId={}, operator={}, ttl={}, dataType={}, payload={}",
				deviceId, paramDTO.getProductId(), paramDTO.getOperator(), paramDTO.getTtl(),
				DATA_TYPE_HEX, payload);

		String body = execute(() -> {
			try {
				return commandClient().CreateCommand(request);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});

		// ② 下发响应：平台返回什么就原样输出
		log.info("设备:{} 【下发·指令响应】{}", deviceId, body);

		JSONObject json = parseOk(body);
		Object result = json.get("result");
		AepCommandResult commandResult = new AepCommandResult();
		if (result instanceof JSONObject) {
			commandResult.setCommandId(((JSONObject) result).getLong("commandId"));
		}
		return commandResult;
	}

	// ========== SDK客户端 ==========

	private AepDeviceCommandClient commandClient() {
		if (commandClient == null) {
			synchronized (this) {
				if (commandClient == null) {
					commandClient = AepDeviceCommandClient.newClient()
							.appKey(aepProperties.getAppKey())
							.appSecret(aepProperties.getAppSecret())
							.build();
				}
			}
		}
		return commandClient;
	}

	// ========== 工具方法 ==========

	private String requireProductId(String productId) {
		if (isBlank(productId)) {
			throw new IllegalArgumentException("缺失必填参数 productId");
		}
		return productId;
	}

	private String requireMasterKey(String masterKey) {
		if (isBlank(masterKey)) {
			throw new IllegalArgumentException("缺失必填参数 masterKey");
		}
		return masterKey;
	}

	private boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}

	private String execute(Supplier<? extends BaseApiResponse> supplier) {
		BaseApiResponse response = supplier.get();
		String body = new String(response.getBody(), StandardCharsets.UTF_8);
		// 原始 HTTP 细节仅失败时补一条；成功由业务侧【下发·指令响应】原样输出 body
		if (response.getStatusCode() != 200) {
			log.warn("AEP HTTP异常 statusCode={}, message={}, body={}", response.getStatusCode(),
					response.getMessage(), body);
		}
		return body;
	}

	private JSONObject parseOk(String body) {
		JSONObject json = JSON.parseObject(body);
		Integer code = json.getInteger("code");
		if (code != null && code != 0) {
			throw new RuntimeException(
					"AEP平台返回错误, code=" + code + ", msg=" + json.getString("msg"));
		}
		return json;
	}
}