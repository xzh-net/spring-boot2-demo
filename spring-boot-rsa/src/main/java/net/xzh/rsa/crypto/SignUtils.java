package net.xzh.rsa.crypto;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Signature;
import java.util.Base64;

/**
 * 签名验签工具类。
 * <p>
 * 同时提供两种签名方案：
 * <ul>
 *   <li>RSA 非对称签名：基于 SHA256withRSA 算法，使用私钥签名、公钥验签，适用于需要身份认证的场景。</li>
 *   <li>HMAC 对称签名：基于 HmacSHA256 算法，签名与验签共用同一密钥，适用于内部服务间的消息完整性校验。</li>
 * </ul>
 * 所有返回的签名字符串均使用 Base64 编码，数据编码统一采用 UTF-8。
 */
public class SignUtils {

    /** RSA 非对称签名算法：SHA256withRSA */
    private static final String RSA_SIGN_ALGO = "SHA256withRSA";
    /** HMAC 对称签名算法：HmacSHA256 */
    private static final String HMAC_ALGO = "HmacSHA256";

    /**
     * 使用 RSA 私钥对数据进行签名。
     *
     * @param data       待签名的原始数据（UTF-8 编码）
     * @param privateKey RSA 私钥
     * @return 签名结果，Base64 编码后的字符串
     * @throws RuntimeException 当签名过程中发生任何异常（如算法不可用、密钥非法等）时抛出
     */
    public static String rsaSign(String data, java.security.PrivateKey privateKey) {
        try {
            Signature signature = Signature.getInstance(RSA_SIGN_ALGO);
            signature.initSign(privateKey);
            signature.update(data.getBytes(StandardCharsets.UTF_8));
            byte[] signBytes = signature.sign();
            return Base64.getEncoder().encodeToString(signBytes);
        } catch (Exception e) {
            throw new RuntimeException("RSA sign failed", e);
        }
    }

    /**
     * 使用 RSA 公钥对签名进行验签。
     *
     * @param data      原始数据（UTF-8 编码）
     * @param sign      Base64 编码的签名字符串
     * @param publicKey RSA 公钥
     * @return {@code true} 表示验签通过，{@code false} 表示签名与数据不匹配
     * @throws RuntimeException 当验签过程中发生任何异常（如算法不可用、密钥非法、签名格式错误等）时抛出
     */
    public static boolean rsaVerify(String data, String sign, java.security.PublicKey publicKey) {
        try {
            Signature signature = Signature.getInstance(RSA_SIGN_ALGO);
            signature.initVerify(publicKey);
            signature.update(data.getBytes(StandardCharsets.UTF_8));
            byte[] signBytes = Base64.getDecoder().decode(sign);
            return signature.verify(signBytes);
        } catch (Exception e) {
            throw new RuntimeException("RSA verify failed", e);
        }
    }

    /**
     * 使用 HMAC 对称密钥对数据进行签名。
     * <p>
     * 签名方与验签方必须共享同一密钥（{@code secret}）才能得到一致的结果。
     *
     * @param data   待签名的原始数据（UTF-8 编码）
     * @param secret HMAC 签名使用的共享密钥（UTF-8 编码）
     * @return 签名结果，Base64 编码后的字符串
     * @throws RuntimeException 当签名过程中发生任何异常（如算法不可用等）时抛出
     */
    public static String hmacSign(String data, String secret) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGO);
            SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGO);
            mac.init(keySpec);
            byte[] signBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(signBytes);
        } catch (Exception e) {
            throw new RuntimeException("HMAC sign failed", e);
        }
    }
}
