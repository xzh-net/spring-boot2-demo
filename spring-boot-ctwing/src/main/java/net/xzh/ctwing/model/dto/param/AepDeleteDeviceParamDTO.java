package net.xzh.ctwing.model.dto.param;

import java.util.List;

import lombok.Data;

/**
 * AEP DeleteDeviceByPost 接口请求体参数（批量删除设备）
 */
@Data
public class AepDeleteDeviceParamDTO {

	/** 产品ID，必填 */
	private Long productId;

	/** 设备ID列表，必填（单次最多200个） */
	private List<String> deviceIdList;
}