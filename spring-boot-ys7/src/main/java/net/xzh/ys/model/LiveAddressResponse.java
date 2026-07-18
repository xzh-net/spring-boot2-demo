package net.xzh.ys.model;

import lombok.Data;

@Data
public class LiveAddressResponse {
    private String code;
    private String msg;
    private LiveAddressData data;

    @Data
    public static class LiveAddressData {
        private String id;
        private String url;
        private String expireTime;
    }
}