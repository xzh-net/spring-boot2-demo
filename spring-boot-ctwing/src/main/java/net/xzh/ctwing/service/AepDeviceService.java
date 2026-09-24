package net.xzh.ctwing.service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Supplier;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ctg.ag.sdk.biz.AepDeviceManagementClient;
import com.ctg.ag.sdk.biz.aep_device_management.CreateDeviceRequest;
import com.ctg.ag.sdk.biz.aep_device_management.DeleteDeviceByPostRequest;
import com.ctg.ag.sdk.biz.aep_device_management.QueryDeviceListRequest;
import com.ctg.ag.sdk.biz.aep_device_management.QueryDeviceRequest;
import com.ctg.ag.sdk.core.model.BaseApiResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.xzh.ctwing.config.AepProperties;
import net.xzh.ctwing.model.dto.param.AepCreatDeviceParamDTO;
import net.xzh.ctwing.model.dto.param.AepDeleteDeviceParamDTO;
import net.xzh.ctwing.model.dto.result.AepApiResult;
import net.xzh.ctwing.model.dto.result.AepDeviceCreateResult;
import net.xzh.ctwing.model.dto.result.AepDeviceInfo;
import net.xzh.ctwing.model.dto.result.AepDevicePageResult;

/**
 * ctwing (电信AEP) 平台设备管理服务
 *
 * <p>覆盖：创建(本地注册编排调用)、查询(列表/详情)、删除(本地取消注册编排调用)。<br>
 * 集成层不感知业务，productId / masterKey 全部由上层显式传入。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AepDeviceService {

	private final AepProperties aepProperties;

	private AepDeviceManagementClient deviceClient;

	// ========== 设备创建 ==========

	/**
	 * 创建单个设备（通用接口，覆盖 LWM2M / MQTT 等可直接入网的协议设备）
	 *
	 * <p>集成层不感知业务：productId / masterKey 由上层（业务编排）解析后显式传入；
	 * 标识字段带 imei 按 LWM2M(NB) 方式创建设备、否则按 deviceSn 方式创建。</p>
	 *
	 * @param productId  产品ID（必填）
	 * @param masterKey  产品MasterKey（必填）
	 * @param deviceName 设备名称（为空时默认取设备标识）
	 * @param deviceSn   设备编码（非LWM2M协议必填）
	 * @param imei       IMEI号（LWM2M/NB 必填，非空时按 NB 方式创建）
	 */
	public AepDeviceCreateResult createDevice(String productId, String masterKey, String deviceName,
			String deviceSn, String imei) {
		boolean nb = !isBlank(imei);
		if (!nb && isBlank(deviceSn)) {
			throw new IllegalArgumentException("创建设备必须提供设备标识：NB 传 imei，4G/其他协议传 deviceSn");
		}

		AepCreatDeviceParamDTO paramDTO = new AepCreatDeviceParamDTO();
		paramDTO.setProductId(Long.valueOf(requireProductId(productId)));
		paramDTO.setOperator(aepProperties.getOperator());
		if (nb) {
			paramDTO.setImei(imei);
			paramDTO.setDeviceName(isBlank(deviceName) ? imei : deviceName);
			AepCreatDeviceParamDTO.Other other = new AepCreatDeviceParamDTO.Other();
			other.setAutoObserver(0);
			paramDTO.setOther(other);
		} else {
			paramDTO.setDeviceSn(deviceSn);
			paramDTO.setDeviceName(isBlank(deviceName) ? deviceSn : deviceName);
		}

		CreateDeviceRequest request = new CreateDeviceRequest();
		request.setParamMasterKey(requireMasterKey(masterKey));
		request.setBody(JSON.toJSONBytes(paramDTO));

		String body = execute(() -> {
			try {
				return deviceClient().CreateDevice(request);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
		log.info("创建设备成功, productId={}, deviceName={}, nb={}, response={}", productId,
				paramDTO.getDeviceName(), nb, body);

		Object result = parseOk(body).get("result");
		if (result instanceof JSONObject) {
			return ((JSONObject) result).toJavaObject(AepDeviceCreateResult.class);
		}
		return null;
	}

	// ========== 设备查询 ==========

	/**
	 * 分页查询设备列表
	 *
	 * @param searchValue 查询关键字：设备名称 / 设备编号 / 设备ID / IMEI（不同协议支持不同）
	 * @param pageNow     当前页数，默认1
	 * @param pageSize    每页记录数，默认20，最大100
	 */
	public AepDevicePageResult queryDeviceList(String productId, String masterKey, String searchValue,
			Integer pageNow, Integer pageSize) {
		QueryDeviceListRequest request = new QueryDeviceListRequest();
		request.setParamMasterKey(requireMasterKey(masterKey));
		request.setParamProductId(Long.valueOf(requireProductId(productId)));
		if (searchValue != null && !searchValue.isEmpty()) {
			request.setParamSearchValue(searchValue);
		}
		request.setParamPageNow(pageNow == null ? 1 : pageNow);
		request.setParamPageSize(pageSize == null ? 20 : pageSize);

		String body = execute(() -> {
			try {
				return deviceClient().QueryDeviceList(request);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
		log.info("查询设备列表成功, searchValue={}, response={}", searchValue, body);

		Object result = parseOk(body).get("result");
		return result instanceof JSONObject ? ((JSONObject) result).toJavaObject(AepDevicePageResult.class)
				: null;
	}

	/**
	 * 查询单个设备详情
	 *
	 * @param deviceId AEP设备ID
	 */
	public AepDeviceInfo queryDevice(String productId, String masterKey, String deviceId) {
		QueryDeviceRequest request = new QueryDeviceRequest();
		request.setParamMasterKey(requireMasterKey(masterKey));
		request.setParamProductId(Long.valueOf(requireProductId(productId)));
		request.setParamDeviceId(deviceId);

		String body = execute(() -> {
			try {
				return deviceClient().QueryDevice(request);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
		log.info("查询设备详情成功, deviceId={}, response={}", deviceId, body);

		Object result = parseOk(body).get("result");
		return result instanceof JSONObject ? ((JSONObject) result).toJavaObject(AepDeviceInfo.class)
				: null;
	}

	/**
	 * 批量删除设备（单次最多200个）
	 */
	public AepApiResult<Object> deleteDevice(String productId, String masterKey, List<String> deviceIds) {
		if (deviceIds == null || deviceIds.isEmpty()) {
			throw new IllegalArgumentException("待删除的 deviceIds 不能为空");
		}

		AepDeleteDeviceParamDTO paramDTO = new AepDeleteDeviceParamDTO();
		paramDTO.setProductId(Long.valueOf(requireProductId(productId)));
		paramDTO.setDeviceIdList(deviceIds);

		DeleteDeviceByPostRequest request = new DeleteDeviceByPostRequest();
		request.setParamMasterKey(requireMasterKey(masterKey));
		request.setBody(JSON.toJSONBytes(paramDTO));

		String body = execute(() -> {
			try {
				return deviceClient().DeleteDeviceByPost(request);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
		log.info("批量删除设备成功, deviceIds={}, response={}", deviceIds, body);
		return parseEnvelope(body);
	}

	// ========== SDK客户端 ==========

	private AepDeviceManagementClient deviceClient() {
		if (deviceClient == null) {
			synchronized (this) {
				if (deviceClient == null) {
					deviceClient = AepDeviceManagementClient.newClient()
							.appKey(aepProperties.getAppKey())
							.appSecret(aepProperties.getAppSecret())
							.build();
				}
			}
		}
		return deviceClient;
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

	/**
	 * 执行SDK调用并返回响应体字符串
	 */
	private String execute(Supplier<? extends BaseApiResponse> supplier) {
		BaseApiResponse response = supplier.get();
		String body = new String(response.getBody(), StandardCharsets.UTF_8);
		log.info("AEP响应 statusCode={}, message={}, body={}", response.getStatusCode(),
				response.getMessage(), body);
		return body;
	}

	/**
	 * 解析AEP响应，业务码非0时抛出异常
	 */
	private JSONObject parseOk(String body) {
		JSONObject json = JSON.parseObject(body);
		Integer code = json.getInteger("code");
		if (code != null && code != 0) {
			throw new RuntimeException(
					"AEP平台返回错误, code=" + code + ", msg=" + json.getString("msg"));
		}
		return json;
	}

	/**
	 * 解析AEP响应为标准信封对象，业务码非0时抛出异常
	 */
	private AepApiResult<Object> parseEnvelope(String body) {
		JSONObject json = parseOk(body);
		AepApiResult<Object> result = new AepApiResult<>();
		result.setCode(json.getInteger("code"));
		result.setMsg(json.getString("msg"));
		result.setResult(json.get("result"));
		return result;
	}
}