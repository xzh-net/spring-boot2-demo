package net.xzh.dify.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import net.xzh.dify.annotation.RequestHeaderResolver;

/**
 * MVC 配置
 * 
 * @author xzh
 *
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
	@Autowired
	private RequestHeaderResolver requestHeaderResolver;

	/**
	 * 注册自定义参数解析
	 */
	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
		resolvers.add(requestHeaderResolver);
	}
}
