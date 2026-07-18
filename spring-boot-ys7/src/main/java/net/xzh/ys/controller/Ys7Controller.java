package net.xzh.ys.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import net.xzh.ys.model.DeviceResponse;
import net.xzh.ys.model.LiveAddressResponse;
import net.xzh.ys.service.Ys7Service;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/ys7")
public class Ys7Controller {

    @Autowired
    private Ys7Service ys7Service;

    /**
     * 获取token
     * @return
     */
    @GetMapping("/token")
    public Map<String, Object> getAccessToken() {
        Map<String, Object> result = new HashMap<>();
        try {
            String accessToken = ys7Service.getAccessToken();
            result.put("code", 200);
            result.put("msg", "操作成功");
            result.put("data", accessToken);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", e.getMessage());
            result.put("data", null);
        }
        return result;
    }

    @GetMapping("/device/list")
    public Map<String, Object> getDeviceList(
            @RequestParam(required = false) Integer pageStart,
            @RequestParam(required = false) Integer pageSize) {
        Map<String, Object> result = new HashMap<>();
        try {
            DeviceResponse deviceResponse;
            if (pageStart != null && pageSize != null) {
                deviceResponse = ys7Service.getDeviceList(pageStart, pageSize);
            } else {
                deviceResponse = ys7Service.getAllDevices();
            }
            result.put("code", 200);
            result.put("msg", "操作成功");
            result.put("data", deviceResponse.getData());
            result.put("page", deviceResponse.getPage());
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", e.getMessage());
            result.put("data", null);
        }
        return result;
    }

    @GetMapping("/device/live/address")
    public Map<String, Object> getLiveAddress(
            @RequestParam String deviceSerial,
            @RequestParam(required = false) Integer channelNo,
            @RequestParam(required = false) Integer protocol,
            @RequestParam(required = false) Integer expireTime,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer quality,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String stopTime) {
        Map<String, Object> result = new HashMap<>();
        try {
            LiveAddressResponse response = ys7Service.getLiveAddress(
                    deviceSerial, channelNo, protocol, expireTime, quality, type, startTime, stopTime);
            result.put("code", 200);
            result.put("msg", "操作成功");
            result.put("data", response.getData());
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", e.getMessage());
            result.put("data", null);
        }
        return result;
    }
}