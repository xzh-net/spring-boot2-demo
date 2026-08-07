package net.xzh.rsa.crypto;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES 对称加密工具类
 * <p>
 * 使用 {@code AES/CBC/PKCS5Padding} 加密模式，提供加密、解密以及密钥和初始向量（IV）的生成功能。
 * 所有方法均为静态方法，可直接通过类名调用，无需实例化。
 * </p>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * String key = AesUtils.generateRandomKey();   // 生成 32 位随机密钥
 * String iv = AesUtils.generateRandomIv();     // 生成 16 位随机 IV
 * String encrypted = AesUtils.encrypt("Hello", key, iv);
 * String decrypted = AesUtils.decrypt(encrypted, key, iv);
 * }</pre>
 */
public class AesUtils {

    /** 加密算法/模式/填充方式 */
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    /** 加密算法名称 */
    private static final String ALGORITHM = "AES";
    /** 初始向量（IV）长度，单位：字节 */
    private static final int IV_LENGTH = 16;

    /**
     * 使用 AES/CBC/PKCS5Padding 模式对明文字符串进行加密
     * <p>
     * 加密结果使用 Base64 编码后返回。
     * </p>
     *
     * @param plainText 待加密的明文字符串
     * @param key       加密密钥，UTF-8 编码，AES-256 需 32 字节
     * @param iv        初始向量（IV），UTF-8 编码，固定 16 字节
     * @return 加密后的密文（Base64 编码字符串）
     * @throws RuntimeException 当加密过程发生异常时包装抛出
     */
    public static String encrypt(String plainText, String key, String iv) {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes(StandardCharsets.UTF_8));
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("AES encrypt failed", e);
        }
    }

    /**
     * 使用 AES/CBC/PKCS5Padding 模式对 Base64 编码的密文进行解密
     *
     * @param cipherText Base64 编码的密文
     * @param key        解密密钥，需与加密时使用的 key 保持一致，UTF-8 编码，AES-256 需 32 字节
     * @param iv         初始向量（IV），需与加密时使用的 iv 保持一致，UTF-8 编码，固定 16 字节
     * @return 解密后的明文字符串（UTF-8 编码）
     * @throws RuntimeException 当解密过程发生异常（如密钥错误、密文被篡改）时包装抛出
     */
    public static String decrypt(String cipherText, String key, String iv) {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes(StandardCharsets.UTF_8));
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            byte[] decoded = Base64.getDecoder().decode(cipherText);
            byte[] decrypted = cipher.doFinal(decoded);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("AES decrypt failed", e);
        }
    }

    /**
     * 生成一个安全的随机 AES 密钥（32 字节）
     * <p>
     * 使用 {@link SecureRandom} 生成 32 字节随机数，经 Base64 编码截断为 32 个字符的密钥字符串，
     * 适用于 AES-256 加密。
     * </p>
     *
     * @return 32 个字符的随机密钥字符串
     */
    public static String generateRandomKey() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getEncoder().withoutPadding().encodeToString(bytes).substring(0, 32);
    }

    /**
     * 生成一个安全的随机初始向量（IV，16 字节）
     * <p>
     * 使用 {@link SecureRandom} 生成 16 字节随机数，经 Base64 编码截断为 16 个字符的 IV 字符串。
     * IV 用于 CBC 模式下增强加密强度，每次加密应使用不同的 IV。
     * </p>
     *
     * @return 16 个字符的随机初始向量字符串
     */
    public static String generateRandomIv() {
        byte[] bytes = new byte[IV_LENGTH];
        new SecureRandom().nextBytes(bytes);
        return Base64.getEncoder().withoutPadding().encodeToString(bytes).substring(0, IV_LENGTH);
    }
}
