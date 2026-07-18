package net.xzh.hikiot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "hikiot")
public class HikiotProperties {
    private String appKey;
    private String appSecret;
    private String userName;
    private String password;
    private String redirectUrl;
    private String deviceSerial;

}