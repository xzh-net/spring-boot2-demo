package net.xzh.rsa.advice;

import net.xzh.rsa.crypto.AesUtils;
import net.xzh.rsa.crypto.SignUtils;
import net.xzh.rsa.dto.CryptoResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 响应体加密切面。
 * <p>
 * 实现 Spring MVC 的 {@link ResponseBodyAdvice} 接口，在 Controller 方法执行后、
 * 响应体写入前自动拦截并加密响应数据。
 * </p>
 * <p>
 * 核心处理流程：
 * <ol>
 *     <li>从 {@link CryptoContext} 中取出当前请求绑定的 AES 密钥</li>
 *     <li>生成随机 IV（初始化向量），保证每次加密结果不同</li>
 *     <li>使用 AES 算法对响应体进行加密</li>
 *     <li>使用 HMAC 对加密后的数据进行签名，防止数据篡改</li>
 *     <li>将密文、IV、签名包装为 {@link CryptoResponse} 返回给客户端</li>
 *     <li>finally 块中清理 {@link CryptoContext}，防止 ThreadLocal 内存泄漏</li>
 * </ol>
 * </p>
 */
@ControllerAdvice
public class EncryptResponseBodyAdvice implements ResponseBodyAdvice<Object> {

	/** Jackson JSON 序列化工具，用于将非 String 类型的响应体转换为 JSON 字符串 */
	private final ObjectMapper objectMapper = new ObjectMapper();

	/**
	 * 判断当前响应是否需要被此 Advice 处理。
	 * <p>
	 * 本方法始终返回 {@code true}，表示拦截所有 Controller 返回的响应体。
	 * 实际是否执行加密逻辑，由 {@link CryptoContext} 中是否存在 AES 密钥决定
	 * （在 {@link #beforeBodyWrite} 中做最终判断）。
	 * </p>
	 *
	 * @param returnType    Controller 方法的返回类型
	 * @param converterType 所选的消息转换器类型
	 * @return {@code true}，表示所有响应体都将经过 {@link #beforeBodyWrite} 处理
	 */
	@Override
	public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
		return true;
	}

	/**
	 * 核心加密逻辑：在响应体写入前执行 AES 加密 + HMAC 签名。
	 * <p>
	 * 具体步骤：
	 * <ol>
	 *     <li>从 {@link CryptoContext} 获取当前请求绑定的 AES 密钥；若为空则直接返回原始响应体</li>
	 *     <li>将响应体序列化为 JSON 字符串（String 类型直接使用，其他类型通过 Jackson 转换）</li>
	 *     <li>调用 {@link AesUtils#generateRandomIv()} 生成随机初始化向量 IV</li>
	 *     <li>调用 {@link AesUtils#encrypt(String, String, String)} 对明文进行 AES 加密</li>
	 *     <li>调用 {@link SignUtils#hmacSign(String, String)} 对密文进行 HMAC 签名</li>
	 *     <li>将密文、IV、签名封装为 {@link CryptoResponse} 对象</li>
	 *     <li>设置 Content-Type 为 application/json</li>
	 *     <li>finally 块中调用 {@link CryptoContext#clear()} 清理 ThreadLocal</li>
	 * </ol>
	 * </p>
	 *
	 * @param body                   Controller 方法返回的原始响应体
	 * @param returnType             Controller 方法的返回类型信息
	 * @param selectedContentType    HTTP 消息转换器选择的内容类型
	 * @param selectedConverterType  选中的消息转换器类
	 * @param request                当前 HTTP 请求
	 * @param response               当前 HTTP 响应
	 * @return 加密后的 {@link CryptoResponse}；若无需加密则返回原始 body
	 * @throws RuntimeException 加密或序列化失败时抛出
	 */
	@Override
	public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
			Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request,
			ServerHttpResponse response) {

		String aesKey = CryptoContext.getAesKey();
		if (aesKey == null) {
			return body;
		}

		try {
			String plainData;
			if (body instanceof String) {
				plainData = (String) body;
			} else {
				plainData = objectMapper.writeValueAsString(body);
			}

			String iv = AesUtils.generateRandomIv();
			String encryptedData = AesUtils.encrypt(plainData, aesKey, iv);
			String sign = SignUtils.hmacSign(encryptedData, aesKey);

			CryptoResponse cryptoResponse = new CryptoResponse(encryptedData, iv, sign);

			response.getHeaders().set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

			return cryptoResponse;
		} catch (Exception e) {
			throw new RuntimeException("Encrypt response failed: " + e.getMessage(), e);
		} finally {
			CryptoContext.clear();
		}
	}
}
