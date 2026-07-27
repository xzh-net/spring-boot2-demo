package net.xzh.generator.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 分页对象
 * 
 * @author xzh
 */
@Data
@Schema(description = "分页对象")
public abstract class BasePage {

	/**
	 * 页码
	 */
	@Schema(description = "页码", example = "1")
	private Integer pageNum = 1;

	/**
	 * 每页数量
	 */
	@Schema(description = "每页数量", example = "10")
	private Integer pageSize = 10;

}