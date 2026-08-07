package net.xzh.rsa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot 启动类
 * <p>
 * 基于 {@link SpringBootApplication} 自动配置启动 RSA+AES 混合加密演示应用。
 * 通过该注解启用自动配置、组件扫描以及 Spring Boot 配置类支持。
 */
@SpringBootApplication
public class RsaAesApplication {

    /**
     * 应用程序入口
     * <p>
     * 通过 {@link SpringApplication#run(Class, String...)} 启动整个 Spring 应用上下文，
     * 加载所有自动配置类及自定义 Bean，并启动内嵌的 Web 容器。
     *
     * @param args 命令行参数，透传给 Spring 应用上下文
     */
    public static void main(String[] args) {
        SpringApplication.run(RsaAesApplication.class, args);
    }
}
