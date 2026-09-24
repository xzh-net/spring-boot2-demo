package net.xzh.ctwing.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import net.xzh.ctwing.model.dto.result.AepDeviceCreateResult;
import net.xzh.ctwing.model.dto.result.AepDeviceInfo;
import net.xzh.ctwing.model.dto.result.AepDevicePageResult;
import net.xzh.ctwing.model.entity.Device;
import net.xzh.ctwing.model.enums.DeviceModel;
import net.xzh.ctwing.model.enums.DeviceType;
import net.xzh.ctwing.model.request.DeviceRequest;
import net.xzh.ctwing.repository.DeviceRepository;

/**
 * 本地设备 CRUD + AEP 注册/取消注册编排
 */
@Service
@RequiredArgsConstructor
public class DeviceService {

	private final DeviceRepository repository;
	private final AepProductResolver productResolver;
	private final AepDeviceService aepDeviceService;

	public List<Device> list(String keyword) {
		return repository.findAll(keyword);
	}

	public Device require(Long id) {
		return repository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("设备不存在: " + id));
	}

	public Device create(DeviceRequest request) {
		validate(request);
		if (repository.findBySn(request.getSn()).isPresent()) {
			throw new IllegalArgumentException("设备编号已存在: " + request.getSn());
		}
		Device entity = toEntity(new Device(), request);
		entity.setRegStatus(0);
		repository.insert(entity);
		return repository.findBySn(request.getSn()).orElse(entity);
	}

	public Device update(Long id, DeviceRequest request) {
		Device existing = require(id);
		validate(request);
		repository.findBySn(request.getSn()).ifPresent(other -> {
			if (!other.getId().equals(id)) {
				throw new IllegalArgumentException("设备编号已存在: " + request.getSn());
			}
		});
		Device entity = toEntity(existing, request);
		// sn 不可改（与 AEP 标识绑定）
		entity.setSn(existing.getSn());
		repository.update(entity);
		return require(id);
	}

	/**
	 * 删除本地库记录；已注册设备必须先取消注册（保证本地与平台一致）
	 */
	public void delete(Long id) {
		Device device = require(id);
		boolean registered = device.getRegStatus() != null && device.getRegStatus() == 1;
		boolean hasDeviceId = device.getDeviceId() != null && !device.getDeviceId().trim().isEmpty();
		if (registered || hasDeviceId) {
			throw new IllegalArgumentException("设备已注册，请先取消注册后再删除");
		}
		repository.deleteById(id);
	}

	/**
	 * 注册：先查 AEP 是否已有该设备
	 * <p>已有 → 直接回写 deviceId；没有 → 调创建接口，成功回写 deviceId。</p>
	 */
	public Device register(Long id) {
		Device device = require(id);
		ProductKey product = productKey(device);

		String foundId = findOnPlatform(product, device);
		if (foundId != null) {
			repository.updateRegStatus(id, 1, foundId);
			return require(id);
		}

		boolean nb = "NB".equalsIgnoreCase(device.getProto());
		// NB：本地 sn 即 IMEI；4G：按 deviceSn 创建
		AepDeviceCreateResult result = aepDeviceService.createDevice(
				product.productId, product.masterKey, device.getName(),
				nb ? null : device.getSn(), nb ? device.getSn() : null);
		if (result == null || result.getDeviceId() == null || result.getDeviceId().trim().isEmpty()) {
			throw new IllegalStateException("AEP 创建设备未返回 deviceId");
		}
		repository.updateRegStatus(id, 1, result.getDeviceId().trim());
		return require(id);
	}

	/**
	 * 取消注册：先查 AEP
	 * <p>有 → 删除平台设备后再清空本地 deviceId；没有 → 直接清空 deviceId/reg_status。</p>
	 */
	public Device unregister(Long id) {
		Device device = require(id);
		ProductKey product = productKey(device);

		String foundId = findOnPlatform(product, device);
		if (foundId != null) {
			aepDeviceService.deleteDevice(product.productId, product.masterKey,
					java.util.Collections.singletonList(foundId));
		}
		// 平台已无该设备 / 删除成功：清空 deviceId + reg_status=0
		repository.updateRegStatus(id, 0, null);
		return require(id);
	}

	/**
	 * AEP 查找设备：优先按本地 deviceId 查详情，再按 sn 搜列表（deviceSn/imei 匹配）
	 *
	 * @return 平台存在的 deviceId；不存在返回 null
	 */
	private String findOnPlatform(ProductKey product, Device device) {
		if (device.getDeviceId() != null && !device.getDeviceId().trim().isEmpty()) {
			try {
				AepDeviceInfo info = aepDeviceService.queryDevice(product.productId, product.masterKey,
						device.getDeviceId().trim());
				if (info != null && info.getDeviceId() != null && !info.getDeviceId().trim().isEmpty()) {
					return info.getDeviceId().trim();
				}
			} catch (Exception e) {
				// 详情查不到则回退按 sn 搜索
			}
		}

		AepDevicePageResult page = aepDeviceService.queryDeviceList(product.productId, product.masterKey,
				device.getSn(), 1, 20);
		if (page == null || page.getList() == null || page.getList().isEmpty()) {
			return null;
		}
		boolean nb = "NB".equalsIgnoreCase(device.getProto());
		for (AepDeviceInfo info : page.getList()) {
			if (info == null || info.getDeviceId() == null || info.getDeviceId().trim().isEmpty()) {
				continue;
			}
			boolean match = nb
					? device.getSn().equals(info.getImei()) || device.getSn().equals(info.getDeviceSn())
					: device.getSn().equals(info.getDeviceSn());
			if (match) {
				return info.getDeviceId().trim();
			}
		}
		return null;
	}

	private void validate(DeviceRequest request) {
		if (request == null || request.getSn() == null || request.getSn().trim().isEmpty()) {
			throw new IllegalArgumentException("设备编号 sn 不能为空");
		}
		if (request.getProto() == null || request.getProto().trim().isEmpty()) {
			throw new IllegalArgumentException("通信方式 proto 不能为空");
		}
		String proto = request.getProto().trim().toUpperCase();
		if (!"4G".equals(proto) && !"NB".equals(proto)) {
			throw new IllegalArgumentException("通信方式仅支持 4G / NB");
		}
		request.setProto(proto);
		if (request.getType() == null || request.getType().trim().isEmpty()) {
			throw new IllegalArgumentException("设备类型 type 不能为空");
		}
		if (request.getModel() == null || request.getModel().trim().isEmpty()) {
			throw new IllegalArgumentException("设备型号 model 不能为空");
		}
		// 校验枚举合法
		DeviceType.from(request.getType());
		DeviceModel.from(request.getModel());
	}

	private Device toEntity(Device entity, DeviceRequest request) {
		entity.setSn(request.getSn().trim());
		entity.setName(request.getName());
		entity.setType(DeviceType.from(request.getType()).name());
		entity.setModel(DeviceModel.from(request.getModel()).name());
		entity.setProto(request.getProto());
		entity.setDeviceId(request.getDeviceId());
		return entity;
	}

	private ProductKey productKey(Device device) {
		AepProductResolver.ProductInfo info = productResolver
				.resolve(DeviceType.from(device.getType()), device.getProto());
		return new ProductKey(info.getProductId(), info.getMasterKey());
	}

	private static final class ProductKey {
		private final String productId;
		private final String masterKey;

		private ProductKey(String productId, String masterKey) {
			this.productId = productId;
			this.masterKey = masterKey;
		}
	}
}
