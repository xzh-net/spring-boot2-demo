package net.xzh.ys.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import net.xzh.ys.config.Ys7Properties;
import net.xzh.ys.model.DeviceResponse;
import net.xzh.ys.model.LiveAddressResponse;
import net.xzh.ys.model.TokenResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class Ys7Service {
	private static final Logger logger = LoggerFactory.getLogger(Ys7Service.class);
	private static final String API_TOKEN_URL = "https://open.ys7.com/api/lapp/token/get";
	private static final String API_DEVICE_LIST_URL = "https://open.ys7.com/api/lapp/device/list";
	private static final String API_LIVE_ADDRESS_URL = "https://open.ys7.com/api/lapp/v2/live/address/get";

	private static final long TOKEN_EXPIRE_BUFFER_MS = 1 * 3600 * 1000L;

	@Autowired
	private Ys7Properties ys7Properties;

	@Autowired
	private RestTemplate restTemplate;

	private volatile TokenHolder tokenHolder;

	private static class TokenHolder {
		String accessToken;
		long expireTime;

		TokenHolder(String accessToken, long expireTime) {
			this.accessToken = accessToken;
			this.expireTime = expireTime;
		}

		boolean isValid() {
			return accessToken != null && !accessToken.isEmpty()
					&& System.currentTimeMillis() < expireTime - TOKEN_EXPIRE_BUFFER_MS;
		}
	}

	private String getAppAccessToken() {
		getAppToken();
		return tokenHolder != null ? tokenHolder.accessToken : null;
	}

	public Map<String, Object> getAppToken() {
		if (tokenHolder != null && tokenHolder.isValid()) {
			logger.info("使用缓存的应用Token");
			Map<String, Object> result = new HashMap<>();
			result.put("accessToken", tokenHolder.accessToken);
			result.put("expireAt", tokenHolder.expireTime);
			return result;
		}

		synchronized (this) {
			if (tokenHolder != null && tokenHolder.isValid()) {
				logger.info("使用缓存的应用Toke（二次检查）");
				Map<String, Object> result = new HashMap<>();
				result.put("accessToken", tokenHolder.accessToken);
				result.put("expireAt", tokenHolder.expireTime);
				return result;
			}

			logger.info("开始获取应用token");

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

			MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
			body.add("appKey", ys7Properties.getAppKey());
			body.add("appSecret", ys7Properties.getAppSecret());

			HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

			logger.info("POST请求 - URL: {}, params: {}", API_TOKEN_URL, requestEntity);
			try {
				ResponseEntity<TokenResponse> responseEntity = restTemplate.postForEntity(API_TOKEN_URL, requestEntity,
						TokenResponse.class);

				TokenResponse response = responseEntity.getBody();
				logger.info("获取萤石数据成功,{}", response);

				if (response != null && "200".equals(response.getCode())) {
					String accessToken = response.getData().getAccessToken();
					Long expireTime = response.getData().getExpireTime();
					tokenHolder = new TokenHolder(accessToken, expireTime);

					Map<String, Object> result = new HashMap<>();
					result.put("accessToken", accessToken);
					result.put("expireAt", expireTime);

					logger.info("accessToken: {} ", accessToken);
					logger.info("expireTime: {} ", expireTime);

					logger.info("结束获取应用token");
					return result;
				} else {
					String errMsg = response != null ? response.getMsg() : "接口返回空";
					logger.error("获取accessToken失败: {}", errMsg);
					throw new RuntimeException("获取accessToken失败: " + errMsg);
				}
			} catch (Exception e) {
				logger.error("调用Token接口异常", e);
				throw new RuntimeException("调用Token接口异常", e);
			}
		}
	}

	public DeviceResponse getDeviceList(Integer pageStart, Integer pageSize) {
		logger.info("开始查询设备列表，page={}, size={}", pageStart, pageSize);

		String accessToken = getAppAccessToken();

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
		body.add("accessToken", accessToken);
		if (pageStart != null) {
			body.add("pageStart", pageStart.toString());
		}
		if (pageSize != null) {
			body.add("pageSize", pageSize.toString());
		}

		HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

		logger.info("POST请求 - URL: {}, params: {}", API_DEVICE_LIST_URL, requestEntity);
		try {
			ResponseEntity<DeviceResponse> responseEntity = restTemplate.postForEntity(API_DEVICE_LIST_URL,
					requestEntity, DeviceResponse.class);

			DeviceResponse response = responseEntity.getBody();
			logger.info("获取萤石数据成功,{}", response);

			if (response != null && "200".equals(response.getCode())) {

				logger.info("查询设备列表成功，第 {} 页，共 {} 条记录", pageStart,
						response.getData() != null ? response.getData().size(): 0);
				return response;
			} else {
				String errMsg = response != null ? response.getMsg() : "接口返回空";
				logger.error("查询设备列表失败: {}", errMsg);
				throw new RuntimeException("查询设备列表失败: " + errMsg);
			}
		} catch (Exception e) {
			logger.error("调用设备列表接口异常", e);
			throw new RuntimeException("调用设备列表接口异常", e);
		}
	}

	public DeviceResponse getAllDevices() {
		logger.info("开始查询所有设备");

		int pageStart = 0;
		int pageSize = 50;
		List<DeviceResponse.Device> allDevices = new ArrayList<>();

		while (true) {
			DeviceResponse response = getDeviceList(pageStart, pageSize);
			List<DeviceResponse.Device> devices = response.getData();

			if (devices == null || devices.isEmpty()) {
				break;
			}

			allDevices.addAll(devices);
			if (devices.size() < pageSize) {
				break;
			}

			pageStart++;
		}

		DeviceResponse result = new DeviceResponse();
		result.setCode("200");
		result.setMsg("操作成功");
		result.setData(allDevices);

		DeviceResponse.PageInfo pageInfo = new DeviceResponse.PageInfo();
		pageInfo.setTotal(allDevices.size());
		pageInfo.setSize(allDevices.size());
		pageInfo.setPage(0);
		result.setPage(pageInfo);

		logger.info("查询所有设备完成，共获取到 {} 条设备数据", allDevices.size());
		return result;
	}

	public LiveAddressResponse getLiveAddress(String deviceSerial, Integer channelNo, Integer protocol,
			Integer expireTime, Integer quality, Integer type, String startTime, String stopTime) {
		logger.info("开始获取设备播放地址，deviceSerial={}, channelNo={}, protocol={}", deviceSerial, channelNo, protocol);

		String accessToken = getAppAccessToken();

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
		body.add("accessToken", accessToken);
		body.add("deviceSerial", deviceSerial);
		if (channelNo != null) {
			body.add("channelNo", channelNo.toString());
		}
		if (protocol != null) {
			body.add("protocol", protocol.toString());
		}
		if (expireTime != null) {
			body.add("expireTime", expireTime.toString());
		}
		if (quality != null) {
			body.add("quality", quality.toString());
		}
		if (type != null) {
			body.add("type", type.toString());
		}
		if (startTime != null) {
			body.add("startTime", startTime);
		}
		if (stopTime != null) {
			body.add("stopTime", stopTime);
		}

		HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

		logger.info("POST请求 - URL: {}, params: {}", API_LIVE_ADDRESS_URL, requestEntity);
		try {
			ResponseEntity<LiveAddressResponse> responseEntity = restTemplate.postForEntity(API_LIVE_ADDRESS_URL,
					requestEntity, LiveAddressResponse.class);

			LiveAddressResponse response = responseEntity.getBody();
			logger.info("获取萤石数据成功,{}", response);

			if (response != null && "200".equals(response.getCode())) {
				logger.info("获取设备播放地址成功，url={}", response.getData() != null ? response.getData().getUrl() : null);
				return response;
			} else {
				String errMsg = response != null ? response.getMsg() : "接口返回空";
				logger.error("获取设备播放地址失败: {}", errMsg);
				throw new RuntimeException("获取设备播放地址失败: " + errMsg);
			}
		} catch (Exception e) {
			logger.error("调用播放地址接口异常", e);
			throw new RuntimeException("调用播放地址接口异常", e);
		}
	}

	public void cleanToken() {
		logger.info("开始重置所有token");
		tokenHolder = null;
		logger.info("结束重置所有token");
	}
}