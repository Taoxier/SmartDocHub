package com.taoxier.smartdochub.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 百度AI配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "baidu.ai")
public class BaiduAiProperties {

    private String appId;

    private String apiKey;

    private String secretKey;

    private String keywordUrl;
}
