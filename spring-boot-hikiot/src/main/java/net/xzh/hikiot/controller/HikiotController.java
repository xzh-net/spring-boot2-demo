package net.xzh.hikiot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import net.xzh.hikiot.service.HikiotService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/hikiot")
public class HikiotController {

    @Autowired
    private HikiotService hikiotService;

    @GetMapping("/token")
    public Map<String, Object> getAccessToken() {
        Map<String, Object> result = new HashMap<>();
        try {
            String token = hikiotService.getToken();
            result.put("code", 200);
            result.put("msg", "获取应用Token成功");
            result.put("data", token);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", e.getMessage());
            result.put("data", null);
        }
        return result;
    }

    @GetMapping("/user/token")
    public Map<String, Object> getUserAccessToken() {
        Map<String, Object> result = new HashMap<>();
        try {
            String token = hikiotService.getUserAccessToken();
            result.put("code", 200);
            result.put("msg", "获取用户Token成功");
            result.put("data", token);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", e.getMessage());
            result.put("data", null);
        }
        return result;
    }

    @GetMapping("/resource")
    public Map<String, Object> getResourcesData(
            @RequestParam(required = false) String deviceSerial,
            @RequestParam(required = false) String channelNo) {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> data = hikiotService.getResourcesData(deviceSerial, channelNo);
            result.put("code", 200);
            result.put("msg", "获取资源详情成功");
            result.put("data", data);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", e.getMessage());
            result.put("data", null);
        }
        return result;
    }

    @GetMapping("/device/token")
    public Map<String, Object> getTokensData(
            @RequestParam(required = false) String deviceSerial,
            @RequestParam(required = false) Integer channelNo) {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> data = hikiotService.getTokensData(deviceSerial, channelNo);
            result.put("code", 200);
            result.put("msg", "获取设备Token成功");
            result.put("data", data);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", e.getMessage());
            result.put("data", null);
        }
        return result;
    }

    @GetMapping("/device/capacity")
    public Map<String, Object> getCapacitysData(
            @RequestParam(required = false) String deviceSerial,
            @RequestParam(required = false) String channelNo) {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> data = hikiotService.getCapacitysData(deviceSerial, channelNo);
            result.put("code", 200);
            result.put("msg", "获取设备能力集成功");
            result.put("data", data);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", e.getMessage());
            result.put("data", null);
        }
        return result;
    }

    @GetMapping("/ops/token")
    public Map<String, Object> getEzvizData() {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> data = hikiotService.getEzvizData();
            result.put("code", 200);
            result.put("msg", "获取非设备Token成功");
            result.put("data", data);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", e.getMessage());
            result.put("data", null);
        }
        return result;
    }

    @GetMapping("/token/refresh")
    public Map<String, Object> refreshAppToken(
            @RequestParam String appAccessToken,
            @RequestParam String refreshAppToken) {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> data = hikiotService.refreshAppToken(appAccessToken, refreshAppToken);
            result.put("code", 200);
            result.put("msg", "刷新应用Token成功");
            result.put("data", data);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", e.getMessage());
            result.put("data", null);
        }
        return result;
    }

    @GetMapping("/user/token/refresh")
    public Map<String, Object> refreshUserToken(
            @RequestParam String appAccessToken,
            @RequestParam String userAccessToken,
            @RequestParam String refreshUserToken) {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> data = hikiotService.refreshUserToken(appAccessToken, userAccessToken, refreshUserToken);
            result.put("code", 200);
            result.put("msg", "刷新用户Token成功");
            result.put("data", data);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", e.getMessage());
            result.put("data", null);
        }
        return result;
    }

    @GetMapping("/device/page")
    public Map<String, Object> getDevicePage(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> data = hikiotService.getDevicePage(page, size);
            result.put("code", 200);
            result.put("msg", "获取设备分页成功");
            result.put("data", data);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", e.getMessage());
            result.put("data", null);
        }
        return result;
    }

    @GetMapping("/device/list")
    public Map<String, Object> getAllDevices() {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> data = hikiotService.getAllDevices();
            result.put("code", 200);
            result.put("msg", "获取设备列表成功");
            result.put("data", data);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", e.getMessage());
            result.put("data", null);
        }
        return result;
    }
}
