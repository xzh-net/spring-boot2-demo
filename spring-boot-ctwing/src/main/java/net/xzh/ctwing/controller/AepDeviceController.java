package net.xzh.ctwing.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import net.xzh.ctwing.model.dto.result.AepDeviceInfo;
import net.xzh.ctwing.model.dto.result.AepDevicePageResult;
import net.xzh.ctwing.model.enums.DeviceType;
import net.xzh.ctwing.model.response.Result;
import net.xzh.ctwing.service.AepDeviceService;
import net.xzh.ctwing.service.AepProductResolver;
import net.xzh.ctwing.service.AepProductResolver.ProductInfo;

/**
 * ctwing (电信AEP) 平台设备查询接口
 *
 * <p>创建走本地设备注册（DeviceService），删除走取消注册；
 * 本 Controller 仅保留平台列表查询 + 单设备详情。<br>
 * 前端只传业务语义（deviceType/protocolType），产品(productId/masterKey)由后台解析。</p>
 */
@RestController
@RequestMapping("/api/aep/device")
@RequiredArgsConstructor
public class AepDeviceController {

	private final AepDeviceService aepDeviceService;
	private final AepProductResolver productResolver;

	/**
	 * 分页查询平台设备列表
	 *
	 * @param deviceType   设备类型（必填），如 BALL_VALVE
	 * @param protocolType 协议类型（必填）：4G / NB
	 * @param searchValue  查询关键字：设备名称/编号/ID/IMEI
	 * @param pageNow      当前页数，默认1
	 * @param pageSize     每页记录数，默认20
	 */
	@GetMapping
	public Result<AepDevicePageResult> queryDeviceList(
			@RequestParam String deviceType,
			@RequestParam String protocolType,
			@RequestParam(required = false) String searchValue,
			@RequestParam(required = false, defaultValue = "1") Integer pageNow,
			@RequestParam(required = false, defaultValue = "20") Integer pageSize) {
		ProductInfo product = resolve(deviceType, protocolType);
		AepDevicePageResult result = aepDeviceService.queryDeviceList(product.getProductId(),
				product.getMasterKey(), searchValue, pageNow, pageSize);
		return result == null ? Result.failed("查询设备列表失败，AEP平台未返回数据")
				: Result.success(result);
	}

	/**
	 * 查询单个设备详情（含 deviceStatus 激活状态、netStatus 在线状态）
	 *
	 * @param deviceId     AEP设备ID
	 * @param deviceType   设备类型（必填），如 BALL_VALVE
	 * @param protocolType 协议类型（必填）：4G / NB
	 */
	@GetMapping("/{deviceId}")
	public Result<AepDeviceInfo> queryDevice(@PathVariable String deviceId,
			@RequestParam String deviceType,
			@RequestParam String protocolType) {
		ProductInfo product = resolve(deviceType, protocolType);
		AepDeviceInfo result = aepDeviceService.queryDevice(product.getProductId(),
				product.getMasterKey(), deviceId);
		return result == null ? Result.failed("查询设备详情失败，AEP平台未返回数据")
				: Result.success(result);
	}

	private ProductInfo resolve(String deviceType, String protocolType) {
		return productResolver.resolve(DeviceType.from(deviceType), protocolType);
	}
}
