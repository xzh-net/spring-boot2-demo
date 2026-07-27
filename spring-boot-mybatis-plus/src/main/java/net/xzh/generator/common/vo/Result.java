package net.xzh.generator.common.vo;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import net.xzh.generator.common.enums.CodeEnum;

/**
 * 通用结果集
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> implements Serializable {

	private static final long serialVersionUID = 1L;
	private T data;
	private Integer code;
	private String message;

	public static <T> Result<T> success(String msg) {
		return of(null, CodeEnum.SUCCESS.getCode(), msg);
	}

	public static <T> Result<T> success(T model, String msg) {
		return of(model, CodeEnum.SUCCESS.getCode(), msg);
	}

	public static <T> Result<T> success(T model) {
		return of(model, CodeEnum.SUCCESS.getCode(), "");
	}

	public static <T> Result<T> of(T data, Integer code, String message) {
		return new Result<>(data, code, message);
	}

	public static <T> Result<T> failed(String msg) {
		return of(null, CodeEnum.ERROR.getCode(), msg);
	}

	public static <T> Result<T> failed(T model, String msg) {
		return of(model, CodeEnum.ERROR.getCode(), msg);
	}
}