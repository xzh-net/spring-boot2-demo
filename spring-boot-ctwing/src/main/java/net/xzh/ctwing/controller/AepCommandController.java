package net.xzh.ctwing.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import net.xzh.ctwing.model.request.IflyTekValveRequest;
import net.xzh.ctwing.model.response.Result;
import net.xzh.ctwing.service.AepCommandService;

/**
 * ctwing (电信AEP) 平台设备控制接口
 *
 * <p>薄 Controller：只做 HTTP 入参/出参包装；组帧、产品解析、下发均在
 * {@link AepCommandService}，Service 可脱离本类独立调用。<br>
 * 前端只传业务语义（deviceType/protocolType），产品(productId/masterKey)由后台解析。</p>
 */
@RestController
@RequestMapping("/api/aep/command")
@RequiredArgsConstructor
public class AepCommandController {

	private final AepCommandService aepCommandService;

	/**
	 * 讯飞球阀：指令下发（慧种田 App 同款报文）
	 * <p>单阀传 dir+open；多头阀传 openlist；返回同时带回本次下发的报文(taskNo/json/hex)</p>
	 * <p>请求体：{@link IflyTekValveRequest}</p>
	 */
	@PostMapping("/iflytek/valve")
	public Result<Map<String, Object>> sendIflyTekValve(@RequestBody IflyTekValveRequest request) {
		Map<String, Object> data = aepCommandService.sendIflyTekValve(request);
		if (data.get("commandId") == null) {
			return Result.failed("下发指令失败，AEP平台未返回 commandId");
		}
		return Result.success(data);
	}
}