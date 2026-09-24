package net.xzh.ctwing.common;

import lombok.extern.slf4j.Slf4j;
import net.xzh.ctwing.model.response.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理，统一转换为 {@link Result} 响应
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(IllegalArgumentException.class)
	public Result<Object> handleIllegalArgument(IllegalArgumentException e) {
		log.warn("参数校验失败: {}", e.getMessage());
		return Result.failed(e.getMessage());
	}

	@ExceptionHandler(RuntimeException.class)
	public Result<Object> handleRuntime(RuntimeException e) {
		log.error("运行时异常", e);
		return Result.failed(e.getMessage() == null ? "系统异常" : e.getMessage());
	}

	@ExceptionHandler(Exception.class)
	public Result<Object> handleException(Exception e) {
		log.error("系统异常", e);
		return Result.failed("系统异常：" + e.getMessage());
	}
}