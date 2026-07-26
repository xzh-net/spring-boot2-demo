package net.xzh.generator.framework.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import net.xzh.generator.framework.config.properties.IgnoreUrlsProperties;

/**
 * Security拦截配置
 * 
 * @author xzh
 *
 */
@Configuration
public class SecurityConfig {

	@Autowired
	private IgnoreUrlsProperties ignoreUrlsProperties;

	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		return http.authorizeHttpRequests(authz -> authz
				// 允许跨域请求的OPTIONS请求
				.antMatchers(HttpMethod.OPTIONS).permitAll()
				.antMatchers(ignoreUrlsProperties.getUrls().toArray(new String[0])).permitAll()
				// 任何请求需要身份认证
				.anyRequest().authenticated())
				// 关闭跨站请求防护及不使用session
				.csrf(csrf -> csrf.disable())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)).build();
	}
}