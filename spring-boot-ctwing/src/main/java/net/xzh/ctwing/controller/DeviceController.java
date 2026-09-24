package net.xzh.ctwing.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import net.xzh.ctwing.model.entity.Device;
import net.xzh.ctwing.model.request.DeviceRequest;
import net.xzh.ctwing.model.response.Result;
import net.xzh.ctwing.service.DeviceService;

/**
 * 本地设备列表接口（SQLite device，基础信息 + 注册状态）
 */
@RestController
@RequestMapping("/api/device")
@RequiredArgsConstructor
public class DeviceController {

	private final DeviceService service;

	/** 设备列表（keyword 可选：sn/name/type/model 模糊） */
	@GetMapping
	public Result<List<Device>> list(
			@RequestParam(required = false) String keyword) {
		return Result.success(service.list(keyword));
	}

	/** 设备详情 */
	@GetMapping("/{id}")
	public Result<Device> detail(@PathVariable Long id) {
		return Result.success(service.require(id));
	}

	/** 新增设备（仅基础信息） */
	@PostMapping
	public Result<Device> create(@RequestBody DeviceRequest request) {
		return Result.success(service.create(request));
	}

	/** 修改设备（仅基础信息） */
	@PutMapping("/{id}")
	public Result<Device> update(@PathVariable Long id,
			@RequestBody DeviceRequest request) {
		return Result.success(service.update(id, request));
	}

	/** 删除设备（仅本地库） */
	@DeleteMapping("/{id}")
	public Result<Void> delete(@PathVariable Long id) {
		service.delete(id);
		return Result.success(null);
	}

	/** 注册到 AEP 平台 */
	@PostMapping("/{id}/register")
	public Result<Device> register(@PathVariable Long id) {
		return Result.success(service.register(id));
	}

	/** 取消注册（AEP 删除 + 本地状态复位） */
	@PostMapping("/{id}/unregister")
	public Result<Device> unregister(@PathVariable Long id) {
		return Result.success(service.unregister(id));
	}
}
