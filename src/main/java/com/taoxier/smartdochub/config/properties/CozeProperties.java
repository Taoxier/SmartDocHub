package com.taoxier.smartdochub.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Coze API配置
 */
@Component
@ConfigurationProperties(prefix = "coze")
public class CozeProperties {

    private String apiKey;
    private String apiUrl = "https://api.coze.cn/v1/chat/completions";

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }
}