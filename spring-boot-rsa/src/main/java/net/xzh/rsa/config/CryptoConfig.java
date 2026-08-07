package net.xzh.rsa.config;

import net.xzh.rsa.crypto.KeyManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 加密组件配置类。
 * <p>
 * 负责向 Spring 容器注册 RSA 相关的加密工具 Bean，
 * 供业务层直接注入使用。
 * </p>
 *
 * @author xzh
 */
@Configuration
public class CryptoConfig {

    /**
     * 创建并注册 {@link KeyManager} Bean。
     * <p>
     * 该 Bean 封装了 RSA 密钥对的加载、缓存与加解密能力，
     * 可被其他组件直接注入调用。
     * </p>
     *
     * @return RSA 密钥管理器实例
     */
    @Bean
    public KeyManager keyManager() {
        return new KeyManager();
    }
}
