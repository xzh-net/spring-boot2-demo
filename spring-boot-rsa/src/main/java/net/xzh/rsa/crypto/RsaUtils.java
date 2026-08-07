package net.xzh.rsa.crypto;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

/**
 * RSA 非对称加密工具类。
 * <p>
 * 使用 {@code RSA/ECB/PKCS1Padding} 工作模式，提供基于公钥加密、私钥解密的能力，
 * 通常用于加密 AES 会话密钥等短小敏感数据。
 */
public class RsaUtils {

    /** RSA 加密/解密的工作模式与填充方式 */
    private static final String TRANSFORMATION = "RSA/ECB/PKCS1Padding";
    /** 加密算法名称 */
    private static final String ALGORITHM = "RSA";

    /**
     * 使用 RSA 公钥对明文字符串进行加密。
     *
     * @param plainText 待加密的明文字符串（UTF-8 编码）
     * @param publicKey RSA 公钥
     * @return 加密后的密文，经过 Base64 编码
     * @throws RuntimeException 当加密过程中发生任何异常时抛出
     */
    public static String encrypt(String plainText, PublicKey publicKey) {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("RSA encrypt failed", e);
        }
    }

    /**
     * 使用 RSA 私钥对 Base64 编码的密文字符串进行解密。
     *
     * @param cipherText Base64 编码的 RSA 密文
     * @param privateKey RSA 私钥
     * @return 解密后的明文字符串（UTF-8 编码）
     * @throws RuntimeException 当解密过程中发生任何异常时抛出
     */
    public static String decrypt(String cipherText, PrivateKey privateKey) {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decoded = Base64.getDecoder().decode(cipherText);
            byte[] decrypted = cipher.doFinal(decoded);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("RSA decrypt failed", e);
        }
    }
}
