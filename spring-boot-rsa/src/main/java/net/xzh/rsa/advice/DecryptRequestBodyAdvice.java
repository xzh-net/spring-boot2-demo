package net.xzh.rsa.advice;

import net.xzh.rsa.crypto.AesUtils;
import net.xzh.rsa.crypto.KeyManager;
import net.xzh.rsa.crypto.RsaUtils;
import net.xzh.rsa.crypto.SignUtils;
import net.xzh.rsa.dto.CryptoRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.InputStreamSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.Enumeration;

/**
 * 请求体解密切面，实现 {@link RequestBodyAdvice} 接口。
 * <p>
 * 在 Controller 方法执行前自动拦截并解密请求体，支持 RSA + AES 混合加密方案。
 * 完整处理流程如下：
 * <ol>
 *   <li>解析请求体为 {@link CryptoRequest} 封装对象</li>
 *   <li>使用 RSA 私钥解密 AES 对称密钥（encryptedKey）</li>
 *   <li>使用解密出的 AES 密钥对密文数据进行 HMAC 签名校验，防止数据篡改</li>
 *   <li>使用 AES 解密实际业务数据（encryptedData + iv）</li>
 *   <li>将解密后的 AES 密钥存入 {@link CryptoContext}，供响应加密阶段复用</li>
 * </ol>
 * 若请求体不是加密格式或不是 application/json 类型，则透传原报文，不做任何处理。
 */
@ControllerAdvice
public class DecryptRequestBodyAdvice implements RequestBodyAdvice {

	/** JSON 序列化/反序列化工具，用于将请求体字符串解析为 CryptoRequest 对象 */
	private final ObjectMapper objectMapper = new ObjectMapper();

	/** 密钥管理器，提供 RSA 私钥用于解密 AES 密钥 */
	@Resource
	private KeyManager keyManager;

	/**
	 * 判断该 Advice 是否支持拦截当前请求。
	 * <p>
	 * 始终返回 {@code true}，表示对所有 Controller 入参生效；
	 * 真正的加密报文判定逻辑在 {@link #beforeBodyRead} 中通过 Content-Type 和 JSON 结构识别。
	 *
	 * @param methodParameter 目标 Controller 方法参数元数据
	 * @param targetType      目标参数类型
	 * @param converterType   选用的 HttpMessageConverter 类型
	 * @return 是否进入拦截链，此处恒为 true
	 */
	@Override
	public boolean supports(MethodParameter methodParameter, Type targetType,
			Class<? extends HttpMessageConverter<?>> converterType) {
		return true;
	}

	/**
	 * 请求体读取前的解密拦截（核心解密逻辑）。
	 * <p>
	 * 处理步骤：
	 * <ol>
	 *   <li>校验 Content-Type 必须包含 application/json，否则直接透传</li>
	 *   <li>读取原始请求体并解析为 {@link CryptoRequest}</li>
	 *   <li>使用 RSA 私钥解密出 AES 密钥</li>
	 *   <li>使用 AES 密钥对密文做 HMAC 签名校验，防止传输中被篡改</li>
	 *   <li>使用 AES + IV 解密密文数据，得到最终的业务 JSON</li>
	 *   <li>将 AES 密钥写入 {@link CryptoContext}，供响应加密时使用</li>
	 *   <li>构造一个新的 {@link HttpInputMessage}，用解密后的明文替换原始加密体</li>
	 * </ol>
	 *
	 * @param inputMessage  原始的 HTTP 输入消息（包含加密请求体）
	 * @param parameter     目标 Controller 方法参数
	 * @param targetType    目标参数类型
	 * @param converterType 选定的消息转换器类型
	 * @return 解密后的 {@link HttpInputMessage}，若不是加密报文则返回原始 inputMessage
	 * @throws IOException                 读取请求体失败
	 * @throws RuntimeException            AES 密钥解密失败或签名校验失败时抛出
	 */
	@Override
	public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter parameter, Type targetType,
			Class<? extends HttpMessageConverter<?>> converterType) throws IOException {
		String contentType = inputMessage.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
		if (contentType == null || !contentType.toLowerCase().contains("application/json")) {
			return inputMessage;
		}

		String rawBody = readBody(inputMessage.getBody());
		if (rawBody == null || rawBody.isEmpty()) {
			return inputMessage;
		}

		CryptoRequest cryptoRequest;
		try {
			cryptoRequest = objectMapper.readValue(rawBody, CryptoRequest.class);
		} catch (Exception e) {
			return inputMessage;
		}

		if (cryptoRequest.getEncryptedKey() == null || cryptoRequest.getEncryptedData() == null) {
			return inputMessage;
		}

		String aesKey;
		try {
			aesKey = RsaUtils.decrypt(cryptoRequest.getEncryptedKey(), keyManager.getPrivateKey());
		} catch (Exception e) {
			throw new RuntimeException("Decrypt AES key failed: " + e.getMessage());
		}

		String expectedSign = SignUtils.hmacSign(cryptoRequest.getEncryptedData(), aesKey);
		if (cryptoRequest.getSign() == null || !expectedSign.equals(cryptoRequest.getSign())) {
			throw new RuntimeException("Request signature verification failed");
		}

		String plainData = AesUtils.decrypt(cryptoRequest.getEncryptedData(), aesKey, cryptoRequest.getIv());

		CryptoContext.setAesKey(aesKey);

		String finalPlainData = plainData;
		return new HttpInputMessage() {
			@Override
			public InputStream getBody() {
				return new java.io.ByteArrayInputStream(
						finalPlainData.getBytes(java.nio.charset.StandardCharsets.UTF_8));
			}

			@Override
			public HttpHeaders getHeaders() {
				return inputMessage.getHeaders();
			}
		};
	}

	/**
	 * 请求体被 HttpMessageConverter 转换为 DTO 之后的回调。
	 * <p>
	 * 本切面的解密工作已在 {@link #beforeBodyRead} 中完成，此处直接透传，不做额外处理。
	 *
	 * @param body          已转换的业务对象
	 * @param inputMessage  HTTP 输入消息
	 * @param parameter     Controller 方法参数
	 * @param targetType    目标类型
	 * @param converterType 消息转换器类型
	 * @return 直接返回原始 body
	 */
	@Override
	public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType,
			Class<? extends HttpMessageConverter<?>> converterType) {
		return body;
	}

	/**
	 * 处理空请求体场景。
	 * <p>
	 * 空体无需解密，直接返回 null 即可。
	 *
	 * @param body          当前 body（通常为 null）
	 * @param inputMessage  HTTP 输入消息
	 * @param parameter     Controller 方法参数
	 * @param targetType    目标类型
	 * @param converterType 消息转换器类型
	 * @return 直接返回原始 body
	 */
	@Override
	public Object handleEmptyBody(Object body, HttpInputMessage inputMessage, MethodParameter parameter,
			Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
		return body;
	}

	/**
	 * 将请求体 {@link InputStream} 完整读取为 UTF-8 字符串。
	 * <p>
	 * 使用 4KB 缓冲区循环读入，避免一次性加载大文件导致 OOM。
	 *
	 * @param inputMessageBody 原始请求体输入流
	 * @return 以 UTF-8 解码后的请求体文本
	 * @throws IOException 读取过程中发生 I/O 异常
	 */
	private String readBody(InputStream inputStream) throws IOException {
		java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
		byte[] data = new byte[4096];
		int n;
		while ((n = inputStream.read(data, 0, data.length)) != -1) {
			buffer.write(data, 0, n);
		}
		return buffer.toString("UTF-8");
	}
}
