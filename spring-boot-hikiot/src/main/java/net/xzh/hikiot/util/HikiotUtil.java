package net.xzh.hikiot.util;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * 海康互联获取数据工具类
 * 
 * @date 2026/7/14
 */
@Slf4j
public class HikiotUtil {

	/**
	 * ==============================基础配置==============================
	 */
	// 应用key
	private static final String appKey = "2076543337980309596";
	// 应用密钥，若开发者应用未开启了数据加密，请将appSecret字段清空
	private static final String appSecret = "U0RU6iq5L5Xa97pnjatxstK5xo+v0M6cX3/84a0oKN7NDweAR1qXXAXucD9jAVdN25EOkLDJD7j6umuuNepQdo93QYjq/vzlJxpMXB2OCJNhzNatQsNy5FfekVqdaScWozpw8BiPDAgMBAAECgYBNDtNyB6np4onZNZr/5WAn9HmG6+TgDZVMZCpe4J9Q0nliPzQdfr6p+801CjPduHhMbqbQvAPJ4o8yDlV5RpqOu/urlPzr6z2qphQRFZlcEEYxWDeRzAJGhspBurlMhMZYQH9Knrhm2uZeH4ijq1En2qUcRGfLKfx2It9o1CfAwQJBAN/BmEApBK0U8pDUVOi9z6bD0rVrw2sI7viCJhM6dlHiPJmAIp6Xj0W4Ren5JopOy0YkcLvWFWli41Qlpl/Uz7ECQQC821AEDcfXIH8iFfbaAL0BH6F0u7u1w6TEmI0BZLCuTb2wmNFicCaLzmG4Uruqx5dqs52OHUQwzOjuE4zQ9VuzAkAjYn4tR09R/8oMVUfINpwsBzO/NPeGabdA3XQf/lLPyDNFIxpWcilaITYNNjV+Ec/bm8+oJMbmD5lbqUiSRxlBAkEAmkMaDPWksthcgF8oj9HikLRhkZR3M3VndR6WrvisSPQ/aayp+5pYIgKmV4VcvZbi28lzfM3zEVmPOkjF0TlZeQJAZwt8mla5fn1xal/ghoaOhRV/pblr3T7KEGfKsYU8V2IzaRj3ss5eyi/j3YDMUnIvG7Tuq9W8dOzKa0RzW5w03g==";
	private static final String redirectUrl = "http://mobile.njher.com/agriculture/hikiot/callback";// 应用回调地址
	private static final String userName = "13998417419"; // 管理员登录账号
	private static final String userPwd = "123456"; // 管理员登录密码
	private static String appAccessToken; // 应用token
	private static String refreshAppToken; // 应用刷新token
	private static String userAccessToken; // 用户token
	private static String refreshUserToken; // 用户刷新token
	private static final String deviceSerial = "FG6768918"; // 查询设备

	/**
	 * ==============================获取视频流相关==============================
	 */
	// 获取应用访问凭证
	public static final String API_TOKEN_URL = "https://open-api.hikiot.com/auth/exchangeAppToken";
	// 刷新应用访问凭证
	public static final String API_REFRESH_TOKEN_URL = "https://open-api.hikiot.com/auth/refreshAppToken";
	// 获取授权码（重要）
	public static final String API_APPLY_AUTH_CODE_URL = "https://open-api.hikiot.com/auth/third/applyAuthCode";
	// 授权码获取用户访问凭证
	public static final String API_CODE2_TOKEN_URL = "https://open-api.hikiot.com/auth/third/code2Token";
	// 刷新用户访问凭证
	public static final String API_REFRESH_USER_TOKEN_URL = "https://open-api.hikiot.com/auth/third/refreshUserToken";
	// 获取非设备操作token
	public static final String API_OPS_TOKEN_URL = "https://open-api.hikiot.com/device/v1/token/ops/get";
	// 获取资源详情
	public static final String API_RESOURCE_URL = "https://open-api.hikiot.com/device/desk/pc/resource/v1/getById";
	// 批量获取设备token
	public static final String API_BATCH_DEVICE_TOKEN_URL = "https://open-api.hikiot.com/device/v1/token/device/batch";
	// 设备/通道能力查询
	public static final String API_DEVICE_CAPACITIES_URL = "https://open-api.hikiot.com/device/v1/getDeviceCapacities";

	/**
	 * ==============================获取其他数据相关==============================
	 */
	// 设备/通道能力查询
	public static final String API_DEVICE_LIST_URL = "https://open-api.hikiot.com/device/v1/page";

	public static void main(String[] args) {
		/**
		 * 1. 获取应用token
		 */
		getToken(appKey, appSecret);
		/**
		 * 2. 刷新应用token
		 */
//		refreshAppToken(appAccessToken,refreshAppToken);

		/**
		 * 获取授权码
		 */
//		getAuthCodeOnly(appKey, userName, userPwd, redirectUrl);
		/**
		 * 获取用户访问凭证
		 */
		getUserAccessToken();

		/**
		 * 获取非操作设备token
		 */
//		getEzvizData();

		/**
		 * 资源详情
		 */
		getResourcesData();

		/**
		 * 单个资源对应的权限信息（使用批量接口查询）
		 */
//		getTokensData();

		/**
		 * 获取能力集
		 */
//		getCapacitysData();
		/**
		 * 设备分页查询
		 */
//		getDevicePage();

	}

	/**
	 * 获取应用token
	 * 
	 * @param appSecret
	 * @param appKey
	 */
	public static void getToken(String appKey, String appSecret) {
		log.info("开始获取应用token");

		Map<String, Object> requestBody = new HashMap<>();
		requestBody.put("appKey", appKey);
		requestBody.put("appSecret", appSecret);
		try {
			Map<String, Object> data = getHikvisionDataPost(API_TOKEN_URL, false, false, requestBody, false);

			if (data != null && data.containsKey("appAccessToken") && data.containsKey("refreshAppToken")) {
				HikiotUtil.appAccessToken = (String) data.get("appAccessToken");
				HikiotUtil.refreshAppToken = (String) data.get("refreshAppToken");
				log.info("appToken: {} ", HikiotUtil.appAccessToken);
				log.info("refreshAppToken: {} ", HikiotUtil.refreshAppToken);
				log.info("结束获取应用token");
			} else {
				log.error("获取Token失败，返回数据格式不正确: {}", data);
			}
		} catch (Exception e) {
			log.error("获取Token异常", e);
		}
	}

	/**
	 * 刷新应用访问凭证
	 * 
	 * @param refreshAppToken
	 * @param appAccessToken
	 */
	private static void refreshAppToken(String appAccessToken, String refreshAppToken) {
		log.info("开始刷新应用访问凭证");

		Map<String, Object> requestBody = new HashMap<>();
		requestBody.put("appAccessToken", appAccessToken);
		requestBody.put("refreshAppToken", refreshAppToken);

		Map<String, Object> data = getHikvisionDataPost(API_REFRESH_TOKEN_URL, false, false, requestBody, false);

		if (data != null && data.containsKey("appAccessToken") && data.containsKey("refreshAppToken")) {
			HikiotUtil.appAccessToken = (String) data.get("appAccessToken");
			HikiotUtil.refreshAppToken = (String) data.get("refreshAppToken");

			log.info("newAppAccessToken: {} ", (String) data.get("appAccessToken"));
			log.info("newRefreshAppToken: {} ", (String) data.get("refreshAppToken"));
			log.info("结束刷新应用访问凭证");
		} else {
			throw new RuntimeException("刷新Token返回数据格式不正确");
		}
	}

	/**
	 * 获取用户访问凭证
	 */
	public static void getUserAccessToken() {
		log.info("开始获取用户访问凭证");

		String authCode = getAuthCodeOnly(appKey, userName, userPwd, redirectUrl);
		if (StringUtils.isBlank(authCode)) {
			throw new RuntimeException("获取authCode失败");
		}

		log.info("获取到authCode: {}", authCode);

		Map<String, Object> params = new HashMap<>();
		params.put("authCode", authCode);

		Map<String, Object> data = getHikvisionDataByGet(API_CODE2_TOKEN_URL, true, false, params, true);

		if (data != null) {
			Map<String, String> userTokens = new HashMap<>();
			userTokens.put("userAccessToken", (String) data.get("userAccessToken"));
			userTokens.put("refreshUserToken", (String) data.get("refreshUserToken"));
			userTokens.put("expiresIn", String.valueOf(data.get("expiresIn")));
			userTokens.put("appKey", (String) data.get("appKey"));

			if (userTokens.get("userAccessToken") != null && userTokens.get("refreshUserToken") != null) {
				HikiotUtil.userAccessToken = (String) data.get("userAccessToken");
				HikiotUtil.refreshUserToken = (String) data.get("refreshUserToken");

				log.info("userAccessToken: {} ", userAccessToken);
				log.info("refreshUserToken: {} ", refreshUserToken);
				log.info("结束获取用户访问凭证");
			}
		}
	}

	/**
	 * 只获取授权码
	 */
	private static String getAuthCodeOnly(String appKey, String userName, String userPwd, String redirectUrl) {
		log.info("开始获取授权码");

		try {
			Map<String, Object> requestBody = new HashMap<>();
			requestBody.put("appKey", appKey);
			requestBody.put("userName", userName);
			requestBody.put("password", userPwd);
			requestBody.put("redirectUrl", redirectUrl);

			log.info("获取authCode请求参数: userName={}, redirectUrl={}", userName, redirectUrl);
			Map<String, Object> data = getHikvisionDataPost(API_APPLY_AUTH_CODE_URL, false, false, requestBody, false);

			if (data != null && data.containsKey("authCode")) {
				log.info("结束获取授权码");
				return (String) data.get("authCode");
			}

			throw new RuntimeException("获取authCode失败，返回数据不正确");
		} catch (Exception e) {
			log.error("获取authCode异常", e);
			throw new RuntimeException("获取authCode异常: " + e.getMessage());
		}
	}

	/**
	 * 获取非操作设备token
	 */
	private static void getEzvizData() {
		log.info("开始获取非设备token");

		Map<String, Object> data = getHikvisionDataByGet(API_OPS_TOKEN_URL, true, true, null, true);

		log.info("结束获取非设备token EzvizData: {} ", data);
	}

	/**
	 * 获取资源详情
	 */
	private static void getResourcesData() {
		log.info("开始获取资源详情");

		Map<String, Object> params = new HashMap<>();
		params.put("deviceSerial", deviceSerial);
		params.put("channelNo", "1");

		Map<String, Object> data = getHikvisionDataByGet(API_RESOURCE_URL, true, true, params, true);

		log.info("结束获取资源详情 ResourceData: {} ", data);
	}

	/**
	 * 单个资源对应的权限信息（使用批量接口查询）
	 */
	private static void getTokensData() {
		log.info("开始单个资源对应的权限信息");

		Map<String, Object> deviceMap = new HashMap<>();
		deviceMap.put("deviceSerial", deviceSerial);
		deviceMap.put("channelNo", 1);
		List<Map<String, Object>> requestBody = Collections.singletonList(deviceMap);

		Map<String, Object> data = getHikvisionDataPost(API_BATCH_DEVICE_TOKEN_URL, true, true, requestBody, true);

		log.info("结束单个资源对应的权限信息 TokensData: {} ", data);
	}

	/**
	 * 获取能力集
	 */
	private static void getCapacitysData() {
		log.info("开始获取能力集");

		Map<String, Object> params = new HashMap<>();
		params.put("deviceSerial", deviceSerial);
		params.put("channelNo", "1");

		Map<String, Object> data = getHikvisionDataByGet(API_DEVICE_CAPACITIES_URL, true, true, params, true);

		log.info("结束获取能力集 ResourceData: {} ", data);
	}

	/**
	 * 设备分页查询，page=50，循环直到本页数据不够50为止
	 */
	/**
	 * 设备分页查询，循环获取所有数据
	 */
	private static void getDevicePage() {
		log.info("开始获取设备分页查询");

		List<Map<String, Object>> allDeviceList = new java.util.ArrayList<>();
		int page = 1;
		int size = 50;
		while (true) {
			Map<String, Object> params = new HashMap<>();
			params.put("page", page);
			params.put("size", size);

			Map<String, Object> data = getHikvisionDataByGet(API_DEVICE_LIST_URL, true, true, params, true);
			try {
				//免费账户并发调用限制
				Thread.sleep(500L);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (data != null && data.containsKey("data")) {
				List<Map<String, Object>> deviceList = (List<Map<String, Object>>) data.get("data");
				log.info("第{}页获取到{}条设备数据", page, deviceList.size());
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
		}
		log.info("结束获取设备分页查询，共获取到{}条设备数据", allDeviceList.size());
	}

	/**
	 * 获取海康互联数据工具方法（核心方法）
	 */
	public static Map<String, Object> getHikvisionDataPost(String API_URL, boolean isToken, boolean isUserToken,
			Object requestBody, boolean needEncrypt) {
		String finalUrl = API_URL;
		if (needEncrypt && StringUtils.isNotBlank(appSecret)) {
			try {
				URI uri = new URI(API_URL);
				if (StringUtils.isNotBlank(uri.getRawQuery())) {
					String encryptedParam = encryptByPrivateKey(uri.getRawQuery(), appSecret);
					String encode = URLEncoder.encode(encryptedParam, StandardCharsets.UTF_8.name());
					String baseUrl = uri.getScheme() + "://" + uri.getAuthority() + uri.getPath();
					finalUrl = baseUrl + "?querySecret=" + encode;
				}
			} catch (Exception e) {
				log.warn("URL参数加密失败: {}", e.getMessage());
			}
		}

		ObjectMapper objectMapper = new ObjectMapper();
		String bodyJson;
		try {
			bodyJson = objectMapper.writeValueAsString(requestBody);
		} catch (Exception e) {
			log.error("序列化请求体失败: {}", e.getMessage());
			throw new RuntimeException("序列化请求体失败", e);
		}
		if (needEncrypt && StringUtils.isNotBlank(appSecret)) {
			String encryptBody;
			try {
				encryptBody = encryptByPrivateKey(bodyJson, appSecret);
			} catch (Exception e) {
				log.error("加密请求体失败: {}", e.getMessage());
				throw new RuntimeException("加密请求体失败", e);
			}
			Map<String, String> bodyMap = new HashMap<>();
			bodyMap.put("bodySecret", encryptBody);
			try {
				bodyJson = objectMapper.writeValueAsString(bodyMap);
			} catch (Exception e) {
				log.error("序列化加密请求体失败: {}", e.getMessage());
				throw new RuntimeException("序列化加密请求体失败", e);
			}
		}

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Content-Type", MediaType.APPLICATION_JSON_VALUE);

		if (isUserToken) {
			if (StringUtils.isBlank(appAccessToken)) {
				throw new RuntimeException("未获取到海康互联App-Access-Token");
			}
			headers.set("App-Access-Token", appAccessToken);

			if (StringUtils.isBlank(userAccessToken)) {
				throw new RuntimeException("未获取到海康互联User-Access-Token");
			}
			headers.set("User-Access-Token", userAccessToken);
		} else if (isToken) {
			if (StringUtils.isBlank(appAccessToken)) {
				throw new RuntimeException("未获取到海康互联App-Access-Token");
			}
			headers.set("App-Access-Token", appAccessToken);
		}

		HttpEntity<String> requestEntity = new HttpEntity<>(bodyJson, headers);

		ResponseEntity<String> response = new RestTemplate().exchange(finalUrl, HttpMethod.POST, requestEntity,
				String.class);

		if (response.getStatusCode() == HttpStatus.OK) {
			String responseBody = response.getBody();
			if (StringUtils.isBlank(responseBody)) {
				throw new RuntimeException("响应数据为空");
			}

			Map<String, Object> result;
			try {
				result = objectMapper.readValue(responseBody, Map.class);
			} catch (Exception e) {
				log.error("解析响应数据失败: {}", e.getMessage());
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
						log.error("序列化响应数据失败: {}", e.getMessage());
						throw new RuntimeException("序列化响应数据失败", e);
					}
					String decryptData = null;
					try {
						decryptData = decryptByPrivateKey(dataJson, appSecret);
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					try {
						Object json = objectMapper.readValue(decryptData, Object.class);
						result.put("data", json);
					} catch (Exception e) {
						log.warn("解析解密数据失败,使用原始字符串: {}", e.getMessage());
						result.put("data", decryptData);
					}
				}
				Object returnData = result.get("data");
				log.info("获取海康互联数据成功,{}", returnData);
				if (returnData instanceof Map) {
					return (Map<String, Object>) returnData;
				} else if (returnData instanceof List) {
					Map<String, Object> returnMap = new HashMap<>();
					returnMap.put("data", returnData);
					return returnMap;
				} else {
					Map<String, Object> returnMap = new HashMap<>();
					returnMap.put("data", returnData);
					return returnMap;
				}
			} else {
				String msg = (String) result.get("msg");
				log.error("获取海康互联数据失败，code: {}, msg: {}", code, msg);
				throw new RuntimeException("获取海康互联数据失败: " + msg);
			}
		} else {
			throw new RuntimeException("请求失败，状态码: " + response.getStatusCode());
		}
	}

	/**
	 * GET请求获取海康互联数据
	 */
	public static Map<String, Object> getHikvisionDataByGet(String API_URL, boolean isToken, boolean isUserToken,
			Map<String, Object> params, boolean needEncrypt) {
		String fullUrl = buildUrlWithParams(API_URL, params);

		log.info("GET请求 - URL: {}, isToken: {}, params: {}", fullUrl, isToken, params);

		String finalUrl = fullUrl;
		if (needEncrypt && StringUtils.isNotBlank(appSecret)) {
			try {
				URI uri = new URI(fullUrl);
				if (StringUtils.isNotBlank(uri.getRawQuery())) {
					String encryptedParam = encryptByPrivateKey(uri.getRawQuery(), appSecret);
					String encode = URLEncoder.encode(encryptedParam, StandardCharsets.UTF_8.name());
					String baseUrl = uri.getScheme() + "://" + uri.getAuthority() + uri.getPath();
					finalUrl = baseUrl + "?querySecret=" + encode;
				}
			} catch (Exception e) {
				log.warn("URL参数加密失败: {}", e.getMessage());
			}
		}

		RequestEntity.HeadersBuilder<?> builder = RequestEntity.get(URI.create(finalUrl));
		builder.header("Content-Type", "application/json");

		if (isUserToken) {
			if (StringUtils.isBlank(appAccessToken)) {
				throw new RuntimeException("未获取到海康互联App-Access-Token");
			}
			builder.header("App-Access-Token", appAccessToken);

			if (StringUtils.isBlank(userAccessToken)) {
				throw new RuntimeException("未获取到海康互联User-Access-Token");
			}
			builder.header("User-Access-Token", userAccessToken);
		} else if (isToken) {
			if (StringUtils.isBlank(appAccessToken)) {
				throw new RuntimeException("未获取到海康互联App-Access-Token");
			}
			builder.header("App-Access-Token", appAccessToken);
		}

		RequestEntity<Void> requestEntity = builder.build();
		ResponseEntity<String> response = new RestTemplate().exchange(requestEntity, String.class);

		if (response.getStatusCode() == HttpStatus.OK) {
			String responseBody = response.getBody();
			if (StringUtils.isBlank(responseBody)) {
				throw new RuntimeException("响应数据为空");
			}

			ObjectMapper objectMapper = new ObjectMapper();
			Map<String, Object> result;
			try {
				result = objectMapper.readValue(responseBody, Map.class);
			} catch (Exception e) {
				log.error("解析响应数据失败: {}", e.getMessage());
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
						log.error("序列化响应数据失败: {}", e.getMessage());
						throw new RuntimeException("序列化响应数据失败", e);
					}
					String decryptData = null;
					try {
						decryptData = decryptByPrivateKey(dataJson, appSecret);
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					try {
						Object json = objectMapper.readValue(decryptData, Object.class);
						result.put("data", json);
					} catch (Exception e) {
						log.warn("解析解密数据失败,使用原始字符串: {}", e.getMessage());
						result.put("data", decryptData);
					}
				}

				log.info("获取海康互联数据成功");

				Object returnData = result.get("data");
				if (returnData instanceof Map) {
					return (Map<String, Object>) returnData;
				} else if (returnData instanceof List) {
					Map<String, Object> returnMap = new HashMap<>();
					returnMap.put("data", returnData);
					return returnMap;
				} else if (returnData != null) {
					Map<String, Object> returnMap = new HashMap<>();
					returnMap.put("data", returnData);
					return returnMap;
				} else {
					return new HashMap<>();
				}
			} else {
				String msg = (String) result.get("msg");
				log.error("获取海康互联数据失败，code: {}, msg: {}", code, msg);
				throw new RuntimeException("获取海康互联数据失败: " + msg);
			}
		} else {
			log.error("请求失败，状态码: {}, 响应: {}", response.getStatusCode(), response.getBody());
			throw new RuntimeException("请求失败，状态码: " + response.getStatusCode());
		}
	}

	/**
	 * 构建带参数的URL
	 */
	private static String buildUrlWithParams(String baseUrl, Map<String, Object> params) {
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
			log.error("构建URL参数失败: {}", e.getMessage());
			throw new RuntimeException("构建URL参数失败", e);
		}
	}

	/**
	 * 加密
	 *
	 * @param data
	 * @param secret
	 * @return 加密后的字符串
	 * @throws Exception
	 */
	private static String encryptByPrivateKey(String data, String secret) throws Exception {
		if (StringUtils.isBlank(secret)) {
			return data;
		}
		RSAPrivateKey privateKey = getPrivateKey(secret);
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.ENCRYPT_MODE, privateKey);
		byte[] dataBytes = URLEncoder.encode(data, "UTF-8").getBytes(StandardCharsets.UTF_8);
		byte[] enBytes = null;
		for (int i = 0; i < dataBytes.length; i += 117) {
			byte[] subBytes = cipher.doFinal(ArrayUtils.subarray(dataBytes, i, i + 117));
			enBytes = ArrayUtils.addAll(enBytes, subBytes);
		}
		return Base64.encodeBase64String(enBytes);
	}

	/**
	 * 解密
	 * 
	 * @param data
	 * @param secret
	 * @return 解密后的字符串
	 * @throws Exception
	 */
	private static String decryptByPrivateKey(String data, String secret) throws Exception {
		if (StringUtils.isBlank(secret)) {
			return data;
		}
		RSAPrivateKey privateKey = getPrivateKey(secret);
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.DECRYPT_MODE, privateKey);
		byte[] dataBytes = Base64.decodeBase64(data.getBytes(StandardCharsets.UTF_8));
		int inputLen = dataBytes.length;
		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			int offset = 0;
			byte[] cache;
			int i = 0;
			while (inputLen - offset > 0) {
				if (inputLen - offset > 128) {
					cache = cipher.doFinal(dataBytes, offset, 128);
				} else {
					cache = cipher.doFinal(dataBytes, offset, inputLen - offset);
				}
				out.write(cache, 0, cache.length);
				i += 1;
				offset = i * 128;
			}
			String outStr = out.toString("UTF-8");
			return URLDecoder.decode(outStr, "UTF-8");
		}
	}

	private static RSAPrivateKey getPrivateKey(String privateKey) throws Exception {
		byte[] keyBytes = Base64.decodeBase64(privateKey);
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory factory = KeyFactory.getInstance("RSA");
		return (RSAPrivateKey) factory.generatePrivate(keySpec);
	}

}