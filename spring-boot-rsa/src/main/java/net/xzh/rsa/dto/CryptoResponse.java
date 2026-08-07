package net.xzh.rsa.dto;

/**
 * 加密响应体 DTO，服务器返回加密响应时使用的传输结构。
 * <p>
 * 服务器使用与请求相同的 AES 密钥对响应数据进行加密，
 * 并通过 HMAC 签名保证数据完整性。
 * </p>
 */
public class CryptoResponse {
    /** AES 加密后的响应数据，Base64 编码 */
    private String encryptedData;
    /** AES 初始向量（IV），Base64 编码 */
    private String iv;
    /** HMAC 签名，用于校验数据完整性 */
    private String sign;

    public CryptoResponse() {
    }

    public CryptoResponse(String encryptedData, String iv, String sign) {
        this.encryptedData = encryptedData;
        this.iv = iv;
        this.sign = sign;
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
