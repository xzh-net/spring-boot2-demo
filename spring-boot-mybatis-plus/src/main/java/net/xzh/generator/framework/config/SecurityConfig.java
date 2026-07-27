package net.xzh.generator.framework.config;

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

	private final IgnoreUrlsProperties ignoreUrlsProperties;

	public SecurityConfig(IgnoreUrlsProperties ignoreUrlsProperties) {
		this.ignoreUrlsProperties = ignoreUrlsProperties;
	}

	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		return http.authorizeHttpRequests(authz -> authz
				.antMatchers(HttpMethod.OPTIONS).permitAll()
				.antMatchers(ignoreUrlsProperties.getUrls().toArray(new String[0])).permitAll()
				.anyRequest().authenticated())
				.csrf(csrf -> csrf.disable())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)).build();
	}
}