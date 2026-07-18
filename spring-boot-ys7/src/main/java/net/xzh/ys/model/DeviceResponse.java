package net.xzh.ys.model;

import lombok.Data;

import java.util.List;

@Data
public class DeviceResponse {
    private String code;
    private String msg;
    private List<Device> data;
    private PageInfo page;

    @Data
    public static class Device {
        private String id;
        private String deviceSerial;
        private String deviceName;
        private String deviceType;
        private Integer status;
        private Integer defence;
        private String deviceVersion;
        private Long addTime;
        private Long updateTime;
        private String parentCategory;
        private Integer riskLevel;
        private String netAddress;
    }

    @Data
    public static class PageInfo {
        private Integer page;
        private Integer size;
        private Integer total;
    }
}