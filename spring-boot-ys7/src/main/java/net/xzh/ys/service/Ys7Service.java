package net.xzh.ys.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import net.xzh.ys.config.Ys7Properties;
import net.xzh.ys.model.DeviceResponse;
import net.xzh.ys.model.LiveAddressResponse;
import net.xzh.ys.model.TokenResponse;

import java.util.ArrayList;
import java.util.List;

@Service
public class Ys7Service {
    private static final Logger logger = LoggerFactory.getLogger(Ys7Service.class);
    private static final String TOKEN_URL = "https://open.ys7.com/api/lapp/token/get";
    private static final String DEVICE_LIST_URL = "https://open.ys7.com/api/lapp/device/list";
    private static final String LIVE_ADDRESS_URL = "https://open.ys7.com/api/lapp/v2/live/address/get";

    @Autowired
    private Ys7Properties ys7Properties;

    @Autowired
    private RestTemplate restTemplate;

    public String getAccessToken() {
        logger.info("开始获取 AccessToken");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("appKey", ys7Properties.getAppKey());
        body.add("appSecret", ys7Properties.getAppSecret());

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<TokenResponse> responseEntity = restTemplate.postForEntity(
                    TOKEN_URL,
                    requestEntity,
                    TokenResponse.class
            );

            TokenResponse response = responseEntity.getBody();
            if (response != null && "200".equals(response.getCode())) {
                String accessToken = response.getData().getAccessToken();
                logger.info("获取 AccessToken 成功");
                return accessToken;
            } else {
                String errMsg = response != null ? response.getMsg() : "接口返回空";
                logger.error("获取 AccessToken 失败: {}", errMsg);
                throw new RuntimeException("获取 AccessToken 失败: " + errMsg);
            }
        } catch (Exception e) {
            logger.error("调用 Token 接口异常", e);
            throw new RuntimeException("调用 Token 接口异常", e);
        }
    }

    public DeviceResponse getDeviceList(Integer pageStart, Integer pageSize) {
        logger.info("开始查询设备列表，pageStart={}, pageSize={}", pageStart, pageSize);

        String accessToken = getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("accessToken", accessToken);
        if (pageStart != null) {
            body.add("pageStart", pageStart.toString());
        }
        if (pageSize != null) {
            body.add("pageSize", pageSize.toString());
        }

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<DeviceResponse> responseEntity = restTemplate.postForEntity(
                    DEVICE_LIST_URL,
                    requestEntity,
                    DeviceResponse.class
            );

            DeviceResponse response = responseEntity.getBody();
            if (response != null && "200".equals(response.getCode())) {
                logger.info("查询设备列表成功，共 {} 条记录",
                        response.getPage() != null ? response.getPage().getTotal() : 0);
                return response;
            } else {
                String errMsg = response != null ? response.getMsg() : "接口返回空";
                logger.error("查询设备列表失败: {}", errMsg);
                throw new RuntimeException("查询设备列表失败: " + errMsg);
            }
        } catch (Exception e) {
            logger.error("调用设备列表接口异常", e);
            throw new RuntimeException("调用设备列表接口异常", e);
        }
    }

    public DeviceResponse getAllDevices() {
        logger.info("开始查询所有设备");

        String accessToken = getAccessToken();
        int pageStart = 0;
        int pageSize = 50;
        List<DeviceResponse.Device> allDevices = new ArrayList<>();

        while (true) {
            DeviceResponse response = getDeviceList(accessToken, pageStart, pageSize);
            List<DeviceResponse.Device> devices = response.getData();

            if (devices == null || devices.isEmpty()) {
                break;
            }

            allDevices.addAll(devices);
            logger.info("第 {} 页获取到 {} 条设备数据", pageStart, devices.size());

            if (devices.size() < pageSize) {
                break;
            }

            pageStart++;
        }

        DeviceResponse result = new DeviceResponse();
        result.setCode("200");
        result.setMsg("操作成功");
        result.setData(allDevices);

        DeviceResponse.PageInfo pageInfo = new DeviceResponse.PageInfo();
        pageInfo.setTotal(allDevices.size());
        pageInfo.setSize(allDevices.size());
        pageInfo.setPage(0);
        result.setPage(pageInfo);

        logger.info("查询所有设备完成，共获取到 {} 条设备数据", allDevices.size());
        return result;
    }

    private DeviceResponse getDeviceList(String accessToken, Integer pageStart, Integer pageSize) {
        logger.info("开始查询设备列表，accessToken={}, pageStart={}, pageSize={}", accessToken != null ? "***" : null, pageStart, pageSize);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("accessToken", accessToken);
        if (pageStart != null) {
            body.add("pageStart", pageStart.toString());
        }
        if (pageSize != null) {
            body.add("pageSize", pageSize.toString());
        }

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<DeviceResponse> responseEntity = restTemplate.postForEntity(
                    DEVICE_LIST_URL,
                    requestEntity,
                    DeviceResponse.class
            );

            DeviceResponse response = responseEntity.getBody();
            if (response != null && "200".equals(response.getCode())) {
                logger.info("查询设备列表成功，共 {} 条记录",
                        response.getPage() != null ? response.getPage().getTotal() : 0);
                return response;
            } else {
                String errMsg = response != null ? response.getMsg() : "接口返回空";
                logger.error("查询设备列表失败: {}", errMsg);
                throw new RuntimeException("查询设备列表失败: " + errMsg);
            }
        } catch (Exception e) {
            logger.error("调用设备列表接口异常", e);
            throw new RuntimeException("调用设备列表接口异常", e);
        }
    }

    public LiveAddressResponse getLiveAddress(String deviceSerial, Integer channelNo, Integer protocol, Integer expireTime, Integer quality, Integer type, String startTime, String stopTime) {
        logger.info("开始获取设备播放地址，deviceSerial={}, channelNo={}, protocol={}", deviceSerial, channelNo, protocol);

        String accessToken = getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("accessToken", accessToken);
        body.add("deviceSerial", deviceSerial);
        if (channelNo != null) {
            body.add("channelNo", channelNo.toString());
        }
        if (protocol != null) {
            body.add("protocol", protocol.toString());
        }
        if (expireTime != null) {
            body.add("expireTime", expireTime.toString());
        }
        if (quality != null) {
            body.add("quality", quality.toString());
        }
        if (type != null) {
            body.add("type", type.toString());
        }
        if (startTime != null) {
            body.add("startTime", startTime);
        }
        if (stopTime != null) {
            body.add("stopTime", stopTime);
        }

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<LiveAddressResponse> responseEntity = restTemplate.postForEntity(
                    LIVE_ADDRESS_URL,
                    requestEntity,
                    LiveAddressResponse.class
            );

            LiveAddressResponse response = responseEntity.getBody();
            if (response != null && "200".equals(response.getCode())) {
                logger.info("获取设备播放地址成功，url={}", response.getData() != null ? response.getData().getUrl() : null);
                return response;
            } else {
                String errMsg = response != null ? response.getMsg() : "接口返回空";
                logger.error("获取设备播放地址失败: {}", errMsg);
                throw new RuntimeException("获取设备播放地址失败: " + errMsg);
            }
        } catch (Exception e) {
            logger.error("调用播放地址接口异常", e);
            throw new RuntimeException("调用播放地址接口异常", e);
        }
    }
}