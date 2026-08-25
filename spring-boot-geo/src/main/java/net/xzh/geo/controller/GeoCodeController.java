package net.xzh.geo.controller;

import lombok.RequiredArgsConstructor;
import net.xzh.geo.common.Result;
import net.xzh.geo.model.GeoCodeResponse;
import net.xzh.geo.service.GeoCodeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/geo")
@RequiredArgsConstructor
public class GeoCodeController {

    private final GeoCodeService geoCodeService;

    @GetMapping("/regeo")
    public Result<GeoCodeResponse> getAddressByLocation(
            @RequestParam String longitude,
            @RequestParam String latitude) {
        GeoCodeResponse response = geoCodeService.getAddressByLocation(longitude, latitude);
        if (response != null && "1".equals(response.getStatus())) {
            return Result.success(response);
        }
        return Result.failed(response != null ? response.getInfo() : "查询失败");
    }

    @GetMapping("/adcode")
    public Result<String> getAdcodeByLocation(
            @RequestParam String longitude,
            @RequestParam String latitude) {
        String adcode = geoCodeService.getAdcodeByLocation(longitude, latitude);
        if (adcode != null) {
            return Result.success(adcode);
        }
        return Result.failed("无法获取行政区划编码");
    }
}