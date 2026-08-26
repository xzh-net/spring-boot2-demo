package net.xzh.geo.service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;

import cn.hutool.http.HttpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.xzh.geo.config.AmapProperties;
import net.xzh.geo.model.GeoCodeResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeoCodeService {

    private final AmapProperties amapProperties;

    public GeoCodeResponse getAddressByLocation(String longitude, String latitude) {
        String location = longitude + "," + latitude;
        String url = amapProperties.getBaseUrl() + "/geocode/regeo";

        Map<String, Object> params = new HashMap<>();
        params.put("key", amapProperties.getKey());
        params.put("location", location);
        params.put("extensions", "all");
        params.put("radius", "1000");
        params.put("roadlevel", "1");

        String response = HttpUtil.get(url, params);
        log.info("高德地图逆地理编码响应: {}", response);
        return JSON.parseObject(response, GeoCodeResponse.class);
    }

    public GeoCodeResponse batchRegeo(String locations) {
        String url = amapProperties.getBaseUrl() + "/geocode/regeo";

        Map<String, Object> params = new HashMap<>();
        params.put("key", amapProperties.getKey());
        try {
            params.put("location", URLEncoder.encode(locations, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        params.put("extensions", "all");
        params.put("batch", "true");
        params.put("radius", "1000");
        params.put("roadlevel", "1");

        String response = HttpUtil.get(url, params);
        log.info("高德批量逆地理编码响应: {}", response);
        return JSON.parseObject(response, GeoCodeResponse.class);
    }
}
