package net.xzh.rsa.dto;

/**
 * 加密请求体 DTO，客户端发送加密请求时使用的传输结构。
 * <p>
 * 采用 RSA + AES 混合加密方案：使用 RSA 加密随机生成的 AES 密钥，
 * 使用 AES 加密实际业务数据，并通过 HMAC 签名保证数据完整性。
 * </p>
 */
public class CryptoRequest {
    /** RSA 加密后的 AES 密钥，Base64 编码 */
    private String encryptedKey;
    /** AES 加密后的业务数据，Base64 编码 */
    private String encryptedData;
    /** AES 初始向量（IV），Base64 编码 */
    private String iv;
    /** HMAC 签名，用于校验数据完整性 */
    private String sign;

    public String getEncryptedKey() {
        return encryptedKey;
    }

    public void setEncryptedKey(String encryptedKey) {
        this.encryptedKey = encryptedKey;
    }

    public String getEncryptedData() {
        return encryptedData;
    }

    public void setEncryptedData(String encryptedData) {
        this.encryptedData = encryptedData;
    }

    public String getIv() {
        return iv;
    }

    public void setIv(String iv) {
        this.iv = iv;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }
}
