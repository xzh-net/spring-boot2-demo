package net.xzh.ys.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "ys7")
public class Ys7Properties {
    private String appKey;
    private String appSecret;

}