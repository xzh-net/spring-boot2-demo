package net.xzh.geo.common;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> implements Serializable {

	private static final long serialVersionUID = 1L;

	private Integer code;
	private String message;
	private T data;
	private Long timestamp = System.currentTimeMillis();

	public static <T> Result<T> success(T data) {
		return new Result<>(200, "success", data, System.currentTimeMillis());
	}

	public static <T> Result<T> failed(String message) {
		return new Result<>(500, message, null, System.currentTimeMillis());
	}
}