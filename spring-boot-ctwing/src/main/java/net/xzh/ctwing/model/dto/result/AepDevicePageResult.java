package net.xzh.ctwing.model.dto.result;

import java.util.List;

import lombok.Data;

/**
 * AEP 平台分页查询设备列表结果
 */
@Data
public class AepDevicePageResult {

	/** 当前页数 */
	private Long pageNum;

	/** 每页记录数 */
	private Long pageSize;

	/** 总记录数 */
	private Long total;

	/** 设备列表 */
	private List<AepDeviceInfo> list;
}