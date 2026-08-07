package net.xzh.rsa.advice;

/**
 * AES 密钥上下文
 * <p>
 * 使用 {@link ThreadLocal} 在一次 HTTP 请求链路中暂存 AES 会话密钥，
 * 由于 ThreadLocal 生命周期与线程绑定而不随请求结束自动释放，
 * 请求处理完成后必须调用 {@link #clear()} 方法清理密钥，防止线程池场景下的内存泄漏和密钥串用问题。
 * </p>
 *
 * @author xzh
 */
public class CryptoContext {

    /**
     * ThreadLocal 变量：在当前线程中保存 AES 会话密钥。
     * <p>
     * 使用 static final 修饰，保证全局唯一且不可变。
     * 每个请求线程通过该变量独立持有自己的密钥副本，避免多线程并发问题。
     * </p>
     */
    private static final ThreadLocal<String> AES_KEY = new ThreadLocal<>();

    /**
     * 设置当前线程的 AES 会话密钥
     *
     * @param key AES 会话密钥字符串（通常为 RSA 解密后得到的 Base64 编码密钥）
     */
    public static void setAesKey(String key) {
        AES_KEY.set(key);
    }

    /**
     * 获取当前线程的 AES 会话密钥
     *
     * @return 当前线程绑定的 AES 会话密钥；如果当前线程尚未设置密钥则返回 {@code null}
     */
    public static String getAesKey() {
        return AES_KEY.get();
    }

    /**
     * 清理当前线程绑定的 AES 会话密钥
     * <p>
     * 必须在请求处理完成（或异常结束）后调用此方法，
     * 否则在线程池复用场景下，密钥会残留在 ThreadLocal 中导致内存泄漏或安全隐患。
     * </p>
     */
    public static void clear() {
        AES_KEY.remove();
    }
}
