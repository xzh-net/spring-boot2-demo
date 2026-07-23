package net.xzh.hikiot.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import net.xzh.hikiot.service.HikiotService;

@RestController
@RequestMapping("/hikiot")
public class HikiotController {

	@Autowired
	private HikiotService hikiotService;

	/**
	 * 操作应用token
	 * 
	 * @return
	 */
	@GetMapping("/token")
	public Map<String, Object> getAccessToken() {
		Map<String, Object> result = new HashMap<>();
		try {
			Map<String, Object> data = hikiotService.getAppToken();
			result.put("code", 200);
			result.put("msg", "操作成功");
			result.put("data", data);
		} catch (Exception e) {
			result.put("code", 500);
			result.put("msg", e.getMessage());
			result.put("data", null);
		}
		return result;
	}

	/**
	 * 刷新应用token
	 * 
	 * @return
	 */
	@PostMapping("/token/refresh")
	public Map<String, Object> refreshAppToken() {
		Map<String, Object> result = new HashMap<>();
		try {
			Map<String, Object> data = hikiotService.refreshAppToken();
			result.put("code", 200);
			result.put("msg", "刷新成功");
			result.put("data", data);
		} catch (Exception e) {
			result.put("code", 500);
			result.put("msg", e.getMessage());
			result.put("data", null);
		}
		return result;
	}

	/**
	 * 操作用户token
	 * 
	 * @return
	 */
	@GetMapping("/user/token")
	public Map<String, Object> getUserAccessToken() {
		Map<String, Object> result = new HashMap<>();
		try {
			Map<String, Object> data = hikiotService.getUserToken();
			result.put("code", 200);
			result.put("msg", "操作成功");
			result.put("data", data);
		} catch (Exception e) {
			result.put("code", 500);
			result.put("msg", e.getMessage());
			result.put("data", null);
		}
		return result;
	}

	/**
	 * 刷新用户token
	 * 
	 * @return
	 */
	@PostMapping("/user/token/refresh")
	public Map<String, Object> refreshUserToken() {
		Map<String, Object> result = new HashMap<>();
		try {
			Map<String, Object> data = hikiotService.refreshUserToken();
			result.put("code", 200);
			result.put("msg", "操作成功");
			result.put("data", data);
		} catch (Exception e) {
			result.put("code", 500);
			result.put("msg", e.getMessage());
			result.put("data", null);
		}
		return result;
	}

	/**
	 * 操作Ezviz数据
	 * 
	 * @return
	 */
	@GetMapping("/ops/token")
	public Map<String, Object> getEzvizData() {
		Map<String, Object> result = new HashMap<>();
		try {
			Map<String, Object> data = hikiotService.getEzvizData();
			result.put("code", 200);
			result.put("msg", "操作成功");
			result.put("data", data);
		} catch (Exception e) {
			result.put("code", 500);
			result.put("msg", e.getMessage());
			result.put("data", null);
		}
		return result;
	}

	/**
	 * 操作设备资源数据
	 * 
	 * @param deviceSerial
	 * @param channelNo
	 * @return
	 */
	@GetMapping("/resource")
	public Map<String, Object> getResourcesData(@RequestParam(required = false) String deviceSerial,
			@RequestParam(required = false) String channelNo) {
		Map<String, Object> result = new HashMap<>();
		try {
			Map<String, Object> data = hikiotService.getResourcesData(deviceSerial, channelNo);
			result.put("code", 200);
			result.put("msg", "操作成功");
			result.put("data", data);
		} catch (Exception e) {
			result.put("code", 500);
			result.put("msg", e.getMessage());
			result.put("data", null);
		}
		return result;
	}

	/**
	 * 操作设备token
	 * 
	 * @param deviceSerial
	 * @param channelNo
	 * @return
	 */
	@GetMapping("/device/token")
	public Map<String, Object> getTokensData(@RequestParam(required = false) String deviceSerial,
			@RequestParam(required = false) Integer channelNo) {
		Map<String, Object> result = new HashMap<>();
		try {
			Map<String, Object> data = hikiotService.getTokensData(deviceSerial, channelNo);
			result.put("code", 200);
			result.put("msg", "操作成功");
			result.put("data", data);
		} catch (Exception e) {
			result.put("code", 500);
			result.put("msg", e.getMessage());
			result.put("data", null);
		}
		return result;
	}

	/**
	 * 操作设备能力数据
	 * 
	 * @param deviceSerial
	 * @param channelNo
	 * @return
	 */
	@GetMapping("/device/capacity")
	public Map<String, Object> getCapacitysData(@RequestParam(required = false) String deviceSerial,
			@RequestParam(required = false) String channelNo) {
		Map<String, Object> result = new HashMap<>();
		try {
			Map<String, Object> data = hikiotService.getCapacitysData(deviceSerial, channelNo);
			result.put("code", 200);
			result.put("msg", "操作成功");
			result.put("data", data);
		} catch (Exception e) {
			result.put("code", 500);
			result.put("msg", e.getMessage());
			result.put("data", null);
		}
		return result;
	}

	/**
	 * 查询设备列表（分页）
	 * 
	 * @param page
	 * @param size
	 * @return
	 */

	@GetMapping("/device/page")
	public Map<String, Object> getDevicePage(@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer size) {
		Map<String, Object> result = new HashMap<>();
		try {
			Map<String, Object> data = hikiotService.getDevicePage(page, size);
			result.put("code", 200);
			result.put("msg", "操作成功");
			result.put("data", data);
		} catch (Exception e) {
			result.put("code", 500);
			result.put("msg", e.getMessage());
			result.put("data", null);
		}
		return result;
	}

	/**
	 * 查询全部设备
	 * 
	 * @return
	 */
	@GetMapping("/device/list")
	public Map<String, Object> getAllDevices() {
		Map<String, Object> result = new HashMap<>();
		try {
			Map<String, Object> data = hikiotService.getAllDevices();
			result.put("code", 200);
			result.put("msg", "操作成功");
			result.put("data", data);
		} catch (Exception e) {
			result.put("code", 500);
			result.put("msg", e.getMessage());
			result.put("data", null);
		}
		return result;
	}
	
	/**
	 * 清空应用token
	 * 
	 * @return
	 */
	@GetMapping("/token/clean")
	public Map<String, Object> cleanToken() {
		Map<String, Object> result = new HashMap<>();
		try {
			hikiotService.cleanToken();
			result.put("code", 200);
			result.put("msg", "操作成功");
			result.put("data", "");
		} catch (Exception e) {
			result.put("code", 500);
			result.put("msg", e.getMessage());
			result.put("data", null);
		}
		return result;
	}
}
