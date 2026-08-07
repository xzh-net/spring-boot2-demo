package net.xzh.rsa.config;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器。
 * <p>
 * 通过 {@link ControllerAdvice} 拦截控制层抛出的所有 {@link Exception}，
 * 将其转换为统一格式的 JSON 响应，避免异常堆栈直接暴露给客户端。
 * </p>
 * <p>
 * 响应结构固定为：
 * <pre>
 * {
 *     "code":    500,
 *     "message": 异常信息,
 *     "data":    null
 * }
 * </pre>
 *
 * @author xzh
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理控制层抛出的任意 {@link Exception}。
     * <p>
     * 捕获异常后，封装为统一的 JSON 结构返回，HTTP 状态码默认为 200，
     * 业务错误码通过 {@code code} 字段区分。
     * </p>
     *
     * @param e 控制层抛出的异常对象
     * @return 统一格式的错误响应，包含 {@code code}、{@code message}、{@code data} 三个字段
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Map<String, Object> handleException(Exception e) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 500);
        result.put("message", e.getMessage());
        result.put("data", null);
        return result;
    }
}
