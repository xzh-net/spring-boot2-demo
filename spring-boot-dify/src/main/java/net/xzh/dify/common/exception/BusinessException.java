package net.xzh.dify.common.exception;

/**
 * 自定义业务异常
 * @author xzh
 *
 */

public class BusinessException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7253259163199901615L;

	public BusinessException(String message) {
		super(message);
	}
}
