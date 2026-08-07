package net.xzh.rsa.crypto;

import javax.annotation.PostConstruct;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * RSA 密钥管理器
 * <p>
 * 作为 Spring Bean 启动时，通过 {@link PostConstruct} 自动生成 2048 位的 RSA 密钥对，
 * 并提供密钥的获取、Base64 编码转换和从 Base64 字符串解析密钥的静态工具方法。
 * </p>
 */
public class KeyManager {

    /** 生成的 RSA 私钥 */
    private PrivateKey privateKey;
    /** 生成的 RSA 公钥 */
    private PublicKey publicKey;

    /**
     * Spring Bean 初始化回调方法
     * <p>
     * 在 Bean 实例化完成后，使用 RSA 算法生成 2048 位的密钥对，
     * 分别赋值给 {@link #privateKey} 和 {@link #publicKey} 字段。
     * </p>
     *
     * @throws IllegalStateException 如果当前运行环境不支持 RSA 算法时抛出
     */
    @PostConstruct
    public void init() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair keyPair = generator.generateKeyPair();
            this.privateKey = keyPair.getPrivate();
            this.publicKey = keyPair.getPublic();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Failed to generate RSA key pair", e);
        }
    }

    /**
     * 获取 RSA 私钥对象
     *
     * @return 初始化时生成的 {@link PrivateKey} 实例
     */
    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    /**
     * 获取 RSA 公钥对象
     *
     * @return 初始化时生成的 {@link PublicKey} 实例
     */
    public PublicKey getPublicKey() {
        return publicKey;
    }

    /**
     * 获取 Base64 编码的 RSA 公钥字符串
     *
     * @return 公钥字节数组经过 Base64 编码后的字符串
     */
    public String getPublicKeyBase64() {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    /**
     * 获取 Base64 编码的 RSA 私钥字符串
     *
     * @return 私钥字节数组经过 Base64 编码后的字符串
     */
    public String getPrivateKeyBase64() {
        return Base64.getEncoder().encodeToString(privateKey.getEncoded());
    }

    /**
     * 将 Base64 编码的公钥字符串解析为 RSA {@link PublicKey} 对象
     *
     * @param base64 公钥的 Base64 编码字符串（X.509 格式）
     * @return 解析后的 RSA 公钥对象
     * @throws IllegalArgumentException 如果 base64 字符串为空、格式错误或无法解析为有效公钥时抛出
     */
    public static PublicKey parsePublicKey(String base64) {
        try {
            byte[] decoded = Base64.getDecoder().decode(base64);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
            return KeyFactory.getInstance("RSA").generatePublic(spec);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid public key", e);
        }
    }

    /**
     * 将 Base64 编码的私钥字符串解析为 RSA {@link PrivateKey} 对象
     *
     * @param base64 私钥的 Base64 编码字符串（PKCS#8 格式）
     * @return 解析后的 RSA 私钥对象
     * @throws IllegalArgumentException 如果 base64 字符串为空、格式错误或无法解析为有效私钥时抛出
     */
    public static PrivateKey parsePrivateKey(String base64) {
        try {
            byte[] decoded = Base64.getDecoder().decode(base64);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
            return KeyFactory.getInstance("RSA").generatePrivate(spec);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid private key", e);
        }
    }
}
