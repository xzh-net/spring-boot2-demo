package net.xzh.ys.model;

import lombok.Data;

@Data
public class TokenResponse {
    private String code;
    private String msg;
    private TokenData data;

    @Data
    public static class TokenData {
        private String accessToken;
        private Long expireTime;
    }
}