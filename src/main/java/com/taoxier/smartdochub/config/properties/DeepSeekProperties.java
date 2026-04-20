package com.taoxier.smartdochub.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * DeepSeek AI配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "deepseek.api")
public class DeepSeekProperties {

    private String key;

    private String url;
}
