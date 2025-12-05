package net.xzh.dify.annotation;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import net.xzh.dify.common.exception.BusinessException;
import net.xzh.dify.dto.HeaderRequest;

@Component
public class RequestHeaderResolver implements HandlerMethodArgumentResolver {

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		// 检查参数是否被 @RequestHeaderParams 注解标记
		return parameter.hasParameterAnnotation(RequestHeader.class)
				&& parameter.getParameterType().equals(HeaderRequest.class);
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		// 创建并返回Header对象
		HeaderRequest headerRequest = new HeaderRequest();
		String authorization = webRequest.getHeader("Authorization");
		String apiId = webRequest.getHeader("Api_Id");
		if (StringUtils.isNotBlank(authorization)) {
			// todo: 转换成用户userid
			headerRequest.setUser_id(authorization); 
		} else {
			throw new BusinessException("用户id不存在");
		}
		if (StringUtils.isNotBlank(apiId)) {
			// todo: 业务id转换成智能体key
			headerRequest.setApi_key(apiId); 
		} else {
			throw new BusinessException("业务id不存在");
		}
		return headerRequest;
	}
}