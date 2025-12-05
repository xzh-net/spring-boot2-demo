package net.xzh.dify.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Dify 客户端配置
 * 
 * @author xzh
 *
 */
@Configuration
public class WebClientConfig {

	@Value("${dify.base-url}")
	private String baseUrl;

	@Bean
	public WebClient webClient() {
		return WebClient.builder().baseUrl(baseUrl).build();
	}
}