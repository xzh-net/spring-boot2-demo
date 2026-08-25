package net.xzh.geo.service;

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

    /**
     * 逆地理编码：通过坐标查询行政区划信息
     *
     * @param longitude 经度
     * @param latitude  纬度
     * @return 包含行政区划编码的响应
     */
    public GeoCodeResponse getAddressByLocation(String longitude, String latitude) {
        String location = longitude + "," + latitude;
        String url = amapProperties.getBaseUrl() + "/geocode/regeo";

        Map<String, Object> params = new HashMap<>();
        params.put("key", amapProperties.getKey());
        params.put("location", location);
        params.put("extensions", "base");
        params.put("radius", "1000");
        params.put("roadlevel", "1");

        String response = HttpUtil.get(url, params);

        log.info("高德地图逆地理编码响应: {}", response);
        return JSON.parseObject(response, GeoCodeResponse.class);
    }

    /**
     * 获取行政区划编码
     *
     * @param longitude 经度
     * @param latitude  纬度
     * @return 行政区划编码
     */
    public String getAdcodeByLocation(String longitude, String latitude) {
        GeoCodeResponse response = getAddressByLocation(longitude, latitude);
        if (response != null && "1".equals(response.getStatus()) 
            && response.getRegeocode() != null 
            && response.getRegeocode().getAddressComponent() != null) {
            return response.getRegeocode().getAddressComponent().getAdcode();
        }
        return null;
    }
}