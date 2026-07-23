package net.xzh.hikiot.service;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.xzh.hikiot.config.HikiotProperties;
import net.xzh.hikiot.util.HikiotUtil;

@Service
public class HikiotService {

	private static final Logger logger = LoggerFactory.getLogger(HikiotService.class);

	private static final String API_TOKEN_URL = "https://open-api.hikiot.com/auth/exchangeAppToken";
	private static final String API_REFRESH_TOKEN_URL = "https://open-api.hikiot.com/auth/refreshAppToken";
	private static final String API_APPLY_AUTH_CODE_URL = "https://open-api.hikiot.com/auth/third/applyAuthCode";
	private static final String API_CODE2_TOKEN_URL = "https://open-api.hikiot.com/auth/third/code2Token";
	private static final String API_REFRESH_USER_TOKEN_URL = "https://open-api.hikiot.com/auth/third/refreshUserAccessToken";
	private static final String API_OPS_TOKEN_URL = "https://open-api.hikiot.com/device/v1/token/ops/get";
	private static final String API_RESOURCE_URL = "https://open-api.hikiot.com/device/desk/pc/resource/v1/getById";
	private static final String API_BATCH_DEVICE_TOKEN_URL = "https://open-api.hikiot.com/device/v1/token/device/batch";
	private static final String API_DEVICE_CAPACITIES_URL = "https://open-api.hikiot.com/device/v1/getDeviceCapacities";
	private static final String API_DEVICE_LIST_URL = "https://open-api.hikiot.com/device/v1/page";

	//token判断有效期阈值，提前1小时
	private static final long TOKEN_EXPIRE_BUFFER_MS = 1 * 3600 * 1000L;

	@Autowired
	private HikiotProperties hikiotProperties;

	@Autowired
	private RestTemplate restTemplate;

	private final ObjectMapper objectMapper = new ObjectMapper();

	private volatile TokenHolder appTokenHolder;
	private volatile TokenHolder userTokenHolder;

	/**
	 * 应用token对象，可以使用redis维护
	 * @author admin
	 *
	 */
	private static class TokenHolder {
		String accessToken;
		String refreshToken;
		long expireAt;

		TokenHolder(String accessToken, String refreshToken, long expireAt) {
			this.accessToken = accessToken;
			this.refreshToken = refreshToken;
			this.expireAt = expireAt;
		}

		boolean isValid() {
			return accessToken != null && !accessToken.isEmpty() && System.currentTimeMillis() < expireAt - TOKEN_EXPIRE_BUFFER_MS;
		}
	}

	/**
	 * 获取应用token字符串
	 * @return
	 */
	private String getAppAccessToken() {
		getAppToken();
		return appTokenHolder != null ? appTokenHolder.accessToken : null;
	}

	/**
	 * 获取用户token字符串
	 * @return
	 */
	private String getUserAccessToken() {
		getUserToken();
		return userTokenHolder != null ? userTokenHolder.accessToken : null;
	}

	/**
	 * 获取应用token对象
	 * @return
	 */
	public Map<String, Object> getAppToken() {
		if (appTokenHolder != null && appTokenHolder.isValid()) {
			logger.info("使用缓存的应用token");
			Map<String, Object> result = new HashMap<>();
			result.put("appAccessToken", appTokenHolder.accessToken);
			result.put("refreshAppToken", appTokenHolder.refreshToken);
			result.put("expireAt", appTokenHolder.expireAt);
			return result;
		}

		synchronized (this) {
			if (appTokenHolder != null && appTokenHolder.isValid()) {
				logger.info("使用缓存的应用token（二次检查）");
				Map<String, Object> result = new HashMap<>();
				result.put("appAccessToken", appTokenHolder.accessToken);
				result.put("refreshAppToken", appTokenHolder.refreshToken);
				result.put("expireAt", appTokenHolder.expireAt);
				return result;
			}

			logger.info("开始获取应用token");

			Map<String, Object> requestBody = new HashMap<>();
			requestBody.put("appKey", hikiotProperties.getAppKey());
			requestBody.put("appSecret", hikiotProperties.getAppSecret());

			try {
				Map<String, Object> data = getHikvisionDataPost(API_TOKEN_URL, null, null, requestBody, false);

				if (data != null && data.containsKey("appAccessToken") && data.containsKey("refreshAppToken")) {
					String appAccessToken = (String) data.get("appAccessToken");
					String refreshAppToken = (String) data.get("refreshAppToken");
					long expireHour = 167;
					if (data.containsKey("expiresIn")) {
						expireHour = NumberUtils.toLong(String.valueOf(data.get("expiresIn")), 167L);
					}
					long expireAt = System.currentTimeMillis() + expireHour * 3600 * 1000L;

					appTokenHolder = new TokenHolder(appAccessToken, refreshAppToken, expireAt);

					logger.info("appToken: {} ", appAccessToken);
					logger.info("refreshAppToken: {} ", refreshAppToken);
					logger.info("结束获取应用token");
					return data;
				} else {
					logger.error("获取Token失败，返回数据格式不正确: {}", data);
					throw new RuntimeException("获取Token失败，返回数据格式不正确");
				}
			} catch (Exception e) {
				logger.error("获取Token异常", e);
				throw new RuntimeException("获取Token异常", e);
			}
		}
	}

	/**
	 * 获取用户token对象
	 * @return
	 */
	public Map<String, Object> getUserToken() {
		if (appTokenHolder == null) {
			throw new RuntimeException("请先获取应用token");
		}

		logger.info("开始获取用户访问凭证");
		
		if (userTokenHolder != null && userTokenHolder.isValid()) {
			logger.info("使用缓存的用户token");
			Map<String, Object> result = new HashMap<>();
			result.put("userAccessToken", userTokenHolder.accessToken);
			result.put("refreshUserToken", userTokenHolder.refreshToken);
			result.put("expireAt", userTokenHolder.expireAt);
			return result;
		}

		synchronized (this) {
			if (userTokenHolder != null && userTokenHolder.isValid()) {
				logger.info("使用缓存的用户token（二次检查）");
				Map<String, Object> result = new HashMap<>();
				result.put("userAccessToken", userTokenHolder.accessToken);
				result.put("refreshUserToken", userTokenHolder.refreshToken);
				result.put("expireAt", userTokenHolder.expireAt);
				return result;
			}

			String appAccessToken = getAppAccessToken();

			String authCode = getAuthCodeOnly(hikiotProperties.getAppKey(), hikiotProperties.getUserName(),
					hikiotProperties.getPassword(), hikiotProperties.getRedirectUrl());
			if (StringUtils.isBlank(authCode)) {
				throw new RuntimeException("获取authCode失败");
			}

			logger.info("获取到authCode: {}", authCode);

			Map<String, Object> params = new HashMap<>();
			params.put("authCode", authCode);

			Map<String, Object> data = getHikvisionDataByGet(API_CODE2_TOKEN_URL, appAccessToken, null, params, true);

			if (data != null && data.containsKey("userAccessToken") && data.containsKey("refreshUserToken")) {
				String userAccessToken = (String) data.get("userAccessToken");
				String refreshUserToken = (String) data.get("refreshUserToken");
				long expireDay = 30;
				if (data.containsKey("expiresIn")) {
					expireDay = NumberUtils.toLong(String.valueOf(data.get("expiresIn")), 30L);
				}
				long expireAt = System.currentTimeMillis() + expireDay * 24 * 3600 * 1000L;

				userTokenHolder = new TokenHolder(userAccessToken, refreshUserToken, expireAt);

				logger.info("userAccessToken: {} ", userAccessToken);
				logger.info("refreshUserToken: {} ", refreshUserToken);
				logger.info("结束获取用户访问凭证");
				return data;
			} else {
				throw new RuntimeException("获取用户Token失败，返回数据格式不正确");
			}
		}
	}

	private String getAuthCodeOnly(String appKey, String userName, String userPwd, String redirectUrl) {
		logger.info("开始获取授权码");

		try {
			Map<String, Object> requestBody = new HashMap<>();
			requestBody.put("appKey", appKey);
			requestBody.put("userName", userName);
			requestBody.put("password", userPwd);
			requestBody.put("redirectUrl", redirectUrl);

			logger.info("获取authCode请求参数: userName={}, redirectUrl={}", userName, redirectUrl);
			Map<String, Object> data = getHikvisionDataPost(API_APPLY_AUTH_CODE_URL, null, null, requestBody, false);

			if (data != null && data.containsKey("authCode")) {
				logger.info("结束获取授权码");
				return (String) data.get("authCode");
			}

			throw new RuntimeException("获取authCode失败，返回数据不正确");
		} catch (Exception e) {
			logger.error("获取authCode异常", e);
			throw new RuntimeException("获取authCode异常: " + e.getMessage());
		}
	}

	public Map<String, Object> getResourcesData(String deviceSerial, String channelNo) {
		logger.info("开始获取资源详情");

		String appAccessToken = getAppAccessToken();
		String userAccessToken = getUserAccessToken();

		Map<String, Object> params = new HashMap<>();
		params.put("deviceSerial", deviceSerial != null ? deviceSerial : hikiotProperties.getDeviceSerial());
		params.put("channelNo", channelNo != null ? channelNo : "1");

		Map<String, Object> data = getHikvisionDataByGet(API_RESOURCE_URL, appAccessToken, userAccessToken, params, true);

		logger.info("结束获取资源详情 ResourceData: {} ", data);
		return data;
	}

	public Map<String, Object> getTokensData(String deviceSerial, Integer channelNo) {
		logger.info("开始获取单个资源对应的权限信息");

		String appAccessToken = getAppAccessToken();
		String userAccessToken = getUserAccessToken();

		Map<String, Object> deviceMap = new HashMap<>();
		deviceMap.put("deviceSerial", deviceSerial != null ? deviceSerial : hikiotProperties.getDeviceSerial());
		deviceMap.put("channelNo", channelNo != null ? channelNo : 1);
		List<Map<String, Object>> requestBody = Collections.singletonList(deviceMap);

		Map<String, Object> data = getHikvisionDataPost(API_BATCH_DEVICE_TOKEN_URL, appAccessToken, userAccessToken, requestBody,
				true);

		logger.info("结束获取单个资源对应的权限信息 TokensData: {} ", data);
		return data;
	}

	public Map<String, Object> getCapacitysData(String deviceSerial, String channelNo) {
		logger.info("开始获取能力集");

		String appAccessToken = getAppAccessToken();
		String userAccessToken = getUserAccessToken();

		Map<String, Object> params = new HashMap<>();
		params.put("deviceSerial", deviceSerial != null ? deviceSerial : hikiotProperties.getDeviceSerial());
		params.put("channelNo", channelNo != null ? channelNo : "1");

		Map<String, Object> data = getHikvisionDataByGet(API_DEVICE_CAPACITIES_URL, appAccessToken, userAccessToken, params, true);

		logger.info("结束获取能力集 CapacityData: {} ", data);
		return data;
	}

	public Map<String, Object> getEzvizData() {
		logger.info("开始获取非设备token");

		String appAccessToken = getAppAccessToken();
		String userAccessToken = getUserAccessToken();

		Map<String, Object> data = getHikvisionDataByGet(API_OPS_TOKEN_URL, appAccessToken, userAccessToken, null, true);

		logger.info("结束获取非设备token EzvizData: {} ", data);
		return data;
	}

	public Map<String, Object> refreshAppToken() {
		if (appTokenHolder == null) {
			throw new RuntimeException("请先获取应用token");
		}
		
		logger.info("开始刷新应用访问凭证");

		Map<String, Object> requestBody = new HashMap<>();
		requestBody.put("appAccessToken", appTokenHolder.accessToken);
		requestBody.put("refreshAppToken", appTokenHolder.refreshToken);

		Map<String, Object> data = getHikvisionDataPost(API_REFRESH_TOKEN_URL, null, null, requestBody, false);

		if (data != null && data.containsKey("appAccessToken") && data.containsKey("refreshAppToken")) {
			String newAppAccessToken = (String) data.get("appAccessToken");
			String newRefreshAppToken = (String) data.get("refreshAppToken");
			long expireHour = 167;
			if (data.containsKey("expiresIn")) {
				expireHour = NumberUtils.toLong(String.valueOf(data.get("expiresIn")), 167L);
			}
			long expireAt = System.currentTimeMillis() + expireHour * 3600 * 1000L;
			
			appTokenHolder = new TokenHolder(newAppAccessToken, newRefreshAppToken, expireAt);

			logger.info("newAppAccessToken: {} ", newAppAccessToken);
			logger.info("newRefreshAppToken: {} ", newRefreshAppToken);
			logger.info("结束刷新应用访问凭证");
			return data;
		} else {
			throw new RuntimeException("刷新Token返回数据格式不正确");
		}
	}

	public Map<String, Object> refreshUserToken() {
		if (userTokenHolder == null) {
			throw new RuntimeException("请先获取用户token");
		}
		
		logger.info("开始刷新用户访问凭证");
		
		String appAccessToken = getAppAccessToken();

		Map<String, Object> requestBody = new HashMap<>();
		requestBody.put("userAccessToken", userTokenHolder.accessToken);
		requestBody.put("refreshUserToken", userTokenHolder.refreshToken);

		Map<String, Object> data = getHikvisionDataPost(API_REFRESH_USER_TOKEN_URL, appAccessToken, null, requestBody,
				true);

		if (data != null && data.containsKey("userAccessToken") && data.containsKey("refreshUserToken")) {
			String newUserAccessToken = (String) data.get("userAccessToken");
			String newRefreshUserToken = (String) data.get("refreshUserToken");
			long expireDay = 30;
			if (data.containsKey("expiresIn")) {
				expireDay = NumberUtils.toLong(String.valueOf(data.get("expiresIn")), 30L);
			}
			long expireAt = System.currentTimeMillis() + expireDay * 24 * 3600 * 1000L;

			userTokenHolder = new TokenHolder(newUserAccessToken, newRefreshUserToken, expireAt);

			logger.info("newUserAccessToken: {} ", newUserAccessToken);
			logger.info("newRefreshUserToken: {} ", newRefreshUserToken);
			logger.info("结束刷新用户访问凭证");
			return data;
		} else {
			throw new RuntimeException("刷新用户Token返回数据格式不正确");
		}
	}

	public Map<String, Object> getDevicePage(Integer page, Integer size) {
		logger.info("开始获取设备分页查询，page={}, size={}", page, size);

		String appToken = getAppAccessToken();
		String userToken = getUserAccessToken();

		return getDevicePageInternal(appToken, userToken, page, size);
	}

	public Map<String, Object> getAllDevices() {
		logger.info("开始查询所有设备");

		String appToken = getAppAccessToken();
		String userToken = getUserAccessToken();

		int page = 1;
		int size = 50;
		List<Map<String, Object>> allDeviceList = new java.util.ArrayList<>();

		while (true) {
			Map<String, Object> data = getDevicePageInternal(appToken, userToken, page, size);

			if (data != null && data.containsKey("data")) {
				List<Map<String, Object>> deviceList = (List<Map<String, Object>>) data.get("data");
				logger.info("第{}页获取到{}条设备数据", page, deviceList.size());

				if (deviceList.isEmpty()) {
					break;
				}

				allDeviceList.addAll(deviceList);

				if (deviceList.size() < size) {
					break;
				}
				page++;
			} else {
				break;
			}

			try {
				// 免费账户并发调用限制
				Thread.sleep(1000L);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				break;
			}
		}

		Map<String, Object> result = new HashMap<>();
		result.put("data", allDeviceList);
		result.put("total", allDeviceList.size());

		logger.info("查询所有设备完成，共获取到{}条设备数据", allDeviceList.size());
		return result;
	}

	private Map<String, Object> getDevicePageInternal(String appToken, String userToken, Integer page, Integer size) {
		logger.info("开始获取设备分页查询，page={}, size={}", page, size);

		Map<String, Object> params = new HashMap<>();
		if (page != null) {
			params.put("page", page);
		}
		if (size != null) {
			params.put("size", size);
		}

		Map<String, Object> data = getHikvisionDataByGet(API_DEVICE_LIST_URL, appToken, userToken, params, true);

		logger.info("结束获取设备分页查询");
		return data;
	}

	private Map<String, Object> getHikvisionDataPost(String API_URL, String appAccessToken, String userAccessToken,
			Object requestBody, boolean needEncrypt) {
		String finalUrl = API_URL;
		String appSecret = hikiotProperties.getAppSecret();

		if (needEncrypt && StringUtils.isNotBlank(appSecret)) {
			try {
				URI uri = new URI(API_URL);
				if (StringUtils.isNotBlank(uri.getRawQuery())) {
					String encryptedParam = HikiotUtil.encryptByPrivateKey(uri.getRawQuery(), appSecret);
					String encode = java.net.URLEncoder.encode(encryptedParam, "UTF-8");
					String baseUrl = uri.getScheme() + "://" + uri.getAuthority() + uri.getPath();
					finalUrl = baseUrl + "?querySecret=" + encode;
				}
			} catch (Exception e) {
				logger.warn("URL参数加密失败: {}", e.getMessage());
			}
		}

		String bodyJson;
		try {
			bodyJson = objectMapper.writeValueAsString(requestBody);
		} catch (Exception e) {
			logger.error("序列化请求体失败: {}", e.getMessage());
			throw new RuntimeException("序列化请求体失败", e);
		}

		if (needEncrypt && StringUtils.isNotBlank(appSecret)) {
			String encryptBody;
			try {
				encryptBody = HikiotUtil.encryptByPrivateKey(bodyJson, appSecret);
			} catch (Exception e) {
				logger.error("加密请求体失败: {}", e.getMessage());
				throw new RuntimeException("加密请求体失败", e);
			}
			Map<String, String> bodyMap = new HashMap<>();
			bodyMap.put("bodySecret", encryptBody);
			try {
				bodyJson = objectMapper.writeValueAsString(bodyMap);
			} catch (Exception e) {
				logger.error("序列化加密请求体失败: {}", e.getMessage());
				throw new RuntimeException("序列化加密请求体失败", e);
			}
		}

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Content-Type", MediaType.APPLICATION_JSON_VALUE);

		if (StringUtils.isNotBlank(userAccessToken)) {
			headers.set("App-Access-Token", appAccessToken);
			headers.set("User-Access-Token", userAccessToken);
		} else if (StringUtils.isNotBlank(appAccessToken)) {
			headers.set("App-Access-Token", appAccessToken);
		}

		HttpEntity<String> requestEntity = new HttpEntity<>(bodyJson, headers);

		ResponseEntity<String> response = restTemplate.exchange(finalUrl, HttpMethod.POST, requestEntity, String.class);

		return parseResponse(response, needEncrypt, appSecret);
	}

	private Map<String, Object> getHikvisionDataByGet(String API_URL, String appAccessToken, String userAccessToken,
			Map<String, Object> params, boolean needEncrypt) {
		String fullUrl = buildUrlWithParams(API_URL, params);
		String appSecret = hikiotProperties.getAppSecret();

		logger.info("GET请求 - URL: {}, params: {}", fullUrl, params);

		String finalUrl = fullUrl;
		if (needEncrypt && StringUtils.isNotBlank(appSecret)) {
			try {
				URI uri = new URI(fullUrl);
				if (StringUtils.isNotBlank(uri.getRawQuery())) {
					String encryptedParam = HikiotUtil.encryptByPrivateKey(uri.getRawQuery(), appSecret);
					String encode = java.net.URLEncoder.encode(encryptedParam, "UTF-8");
					String baseUrl = uri.getScheme() + "://" + uri.getAuthority() + uri.getPath();
					finalUrl = baseUrl + "?querySecret=" + encode;
				}
			} catch (Exception e) {
				logger.warn("URL参数加密失败: {}", e.getMessage());
			}
		}

		RequestEntity.HeadersBuilder<?> builder = RequestEntity.get(URI.create(finalUrl));
		builder.header("Content-Type", "application/json");

		if (StringUtils.isNotBlank(userAccessToken)) {
			builder.header("App-Access-Token", appAccessToken);
			builder.header("User-Access-Token", userAccessToken);
		} else if (StringUtils.isNotBlank(appAccessToken)) {
			builder.header("App-Access-Token", appAccessToken);
		}

		RequestEntity<Void> requestEntity = builder.build();
		ResponseEntity<String> response = restTemplate.exchange(requestEntity, String.class);

		return parseResponse(response, needEncrypt, appSecret);
	}

	private Map<String, Object> parseResponse(ResponseEntity<String> response, boolean needEncrypt, String appSecret) {
		if (response.getStatusCode().is2xxSuccessful()) {
			String responseBody = response.getBody();
			if (StringUtils.isBlank(responseBody)) {
				throw new RuntimeException("响应数据为空");
			}

			Map<String, Object> result;
			try {
				result = objectMapper.readValue(responseBody, Map.class);
			} catch (Exception e) {
				logger.error("解析响应数据失败: {}", e.getMessage());
				throw new RuntimeException("解析响应数据失败", e);
			}

			Integer code = (Integer) result.get("code");
			if (code != null && code.equals(0)) {
				Object data = result.get("data");

				if (data != null && needEncrypt && StringUtils.isNotBlank(appSecret)) {
					String dataJson;
					try {
						dataJson = objectMapper.writeValueAsString(data);
					} catch (Exception e) {
						logger.error("序列化响应数据失败: {}", e.getMessage());
						throw new RuntimeException("序列化响应数据失败", e);
					}
					String decryptData = null;
					try {
						decryptData = HikiotUtil.decryptByPrivateKey(dataJson, appSecret);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					try {
						Object json = objectMapper.readValue(decryptData, Object.class);
						result.put("data", json);
					} catch (Exception e) {
						logger.warn("解析解密数据失败,使用原始字符串: {}", e.getMessage());
						result.put("data", decryptData);
					}
				}

				Object returnData = result.get("data");
				logger.info("获取海康互联数据成功,{}", returnData);

				Map<String, Object> returnMap = new HashMap<>();

				if (returnData instanceof Map) {
					returnMap.putAll((Map<String, Object>) returnData);
				} else {
					returnMap.put("data", returnData);
				}

				for (Map.Entry<String, Object> entry : result.entrySet()) {
					String key = entry.getKey();
					if (!"code".equals(key) && !"msg".equals(key) && !"data".equals(key)) {
						returnMap.put(key, entry.getValue());
					}
				}

				return returnMap;
			} else {
				String msg = (String) result.get("msg");
				logger.error("获取海康互联数据失败，code: {}, msg: {}", code, msg);
				throw new RuntimeException("获取海康互联数据失败: " + msg);
			}
		} else {
			throw new RuntimeException("请求失败，状态码: " + response.getStatusCode());
		}
	}

	private String buildUrlWithParams(String baseUrl, Map<String, Object> params) {
		if (params == null || params.isEmpty()) {
			return baseUrl;
		}

		try {
			UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl);
			for (Map.Entry<String, Object> entry : params.entrySet()) {
				if (entry.getValue() != null) {
					builder.queryParam(entry.getKey(), entry.getValue().toString());
				}
			}
			return builder.build().toUriString();
		} catch (Exception e) {
			logger.error("构建URL参数失败: {}", e.getMessage());
			throw new RuntimeException("构建URL参数失败", e);
		}
	}

	/**
	 * 重置所有token
	 * @return
	 */
	public void cleanToken() {
		logger.info("开始重置所有token");
		
		appTokenHolder = null;
		userTokenHolder = null;
		
		logger.info("结束重置所有token");
		
	}
}