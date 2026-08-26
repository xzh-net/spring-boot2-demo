package net.xzh.geo.controller;

import lombok.RequiredArgsConstructor;
import net.xzh.geo.common.Result;
import net.xzh.geo.model.GeoCodeResponse;
import net.xzh.geo.service.GeoCodeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    @GetMapping("/batch")
    public Result<List<GeoCodeResponse.Regeocode>> batchRegeo(
            @RequestParam String locations) {
        String[] parts = locations.split(";");
        String formatted = String.join("|", parts);

        GeoCodeResponse response = geoCodeService.batchRegeo(formatted.toString());
        if (response != null && "1".equals(response.getStatus())
                && response.getRegeocodes() != null) {
            return Result.success(response.getRegeocodes());
        }
        return Result.failed(response != null ? response.getInfo() : "批量查询失败");
    }
}
