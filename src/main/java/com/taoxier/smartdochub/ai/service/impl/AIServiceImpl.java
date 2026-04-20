package com.taoxier.smartdochub.ai.service.impl;

import com.taoxier.smartdochub.ai.service.AIService;
import com.taoxier.smartdochub.config.properties.BaiduAiProperties;
import com.taoxier.smartdochub.config.properties.DeepSeekProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI服务实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("unchecked")
public class AIServiceImpl implements AIService {

    private final DeepSeekProperties deepSeekProperties;
    private final BaiduAiProperties baiduAiProperties;
    private final RestTemplate restTemplate;

    @Override
    public String classifyDocument(String content) {
        String categories = "技术文档、学术论文、工作报告、商业文档、教育资料、法律文档、医疗健康、金融财经、其他文档";
        String prompt = "请根据以下文档内容，将其分类到以下类别之一：" + categories + "。只返回类别名称，不要返回其他内容。\n" + content;
        String result = executeDeepSeekCall(prompt);
        if (result != null) {
            result = result.trim();
            // 检查分类是否在允许的列表中
            if (categories.contains(result)) {
                return result;
            }
            // 如果返回的分类不在列表中，尝试模糊匹配
            for (String cat : categories.split("、")) {
                if (result.contains(cat) || cat.contains(result)) {
                    return cat;
                }
            }
        }
        return "其他文档";
    }

    @Override
    public List<String> generateTags(String content) {
        List<String> tags = executeBaiduKeywordExtraction(content);
        if (tags != null && !tags.isEmpty()) {
            return tags;
        }
        return new ArrayList<>();
    }

    @Override
    public String generateDescription(String content) {
        String prompt = "请根据以下文档内容，生成一句简短的描述（50-100字），概括文档的核心内容。\n" + content;
        return executeDeepSeekCall(prompt);
    }

    private String executeDeepSeekCall(String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + deepSeekProperties.getKey());

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "deepseek-chat");
            requestBody.put("temperature", 0.6);

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "user", "content", prompt));
            requestBody.put("messages", messages);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(deepSeekProperties.getUrl(), request, Map.class);

            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
                if (!choices.isEmpty()) {
                    Map<String, String> message = (Map<String, String>) choices.get(0).get("message");
                    return message.get("content");
                }
            }
        } catch (Exception e) {
            log.error("DeepSeek AI call failed: {}", e.getMessage());
        }
        return null;
    }

    private List<String> executeBaiduKeywordExtraction(String content) {
        try {
            String accessToken = getBaiduAccessToken();
            if (accessToken == null) {
                log.error("Failed to get Baidu access token");
                return null;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("text", Collections.singletonList(content));
            requestBody.put("num", 10);

            String url = baiduAiProperties.getKeywordUrl() + "?access_token=" + accessToken;
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("results")) {
                List<Map<String, Object>> results = (List<Map<String, Object>>) responseBody.get("results");
                List<String> keywords = new ArrayList<>();
                for (Map<String, Object> result : results) {
                    String keyword = (String) result.get("word");
                    if (keyword != null && !keyword.isEmpty()) {
                        keywords.add(keyword);
                    }
                }
                return keywords;
            }
        } catch (Exception e) {
            log.error("Baidu AI call failed: {}", e.getMessage());
        }
        return null;
    }

    private String getBaiduAccessToken() {
        try {
            String url = "https://aip.baidubce.com/oauth/2.0/token?grant_type=client_credentials" +
                    "&client_id=" + baiduAiProperties.getApiKey() +
                    "&client_secret=" + baiduAiProperties.getSecretKey();

            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("access_token")) {
                return (String) responseBody.get("access_token");
            }
        } catch (Exception e) {
            log.error("Failed to get Baidu access token: {}", e.getMessage());
        }
        return null;
    }
}
