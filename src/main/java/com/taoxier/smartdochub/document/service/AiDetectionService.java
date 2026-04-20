package com.taoxier.smartdochub.document.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.taoxier.smartdochub.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * AI 生成检测服务
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AiDetectionService {

    private final RestTemplate restTemplate;

    // AI 检测服务地址
    private static final String AI_DETECTION_URL = "http://localhost:8000/predict/ai/text";

    /**
     * 检测文本是否由 AI 生成
     * 
     * @param text 文本内容
     * @return AI 生成概率
     */
    public double detectAiGeneratedText(String text) {
        try {
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("text", text);
            requestBody.put("max_length", 512);

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 构建请求实体
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            // 发送请求
            ResponseEntity<String> response = restTemplate.postForEntity(AI_DETECTION_URL, requestEntity, String.class);

            // 解析响应
            JSONObject responseBody = JSON.parseObject(response.getBody());
            return responseBody.getDouble("ai_probability");

        } catch (Exception e) {
            log.error("AI 检测失败: {}", e.getMessage());
            // 发生错误时返回默认值 0.5
            return 0.5;
        }
    }

    /**
     * 检测文档是否由 AI 生成
     * 
     * @param documentId 文档ID
     * @param text       文档文本内容
     * @return AI 生成概率
     */
    public double detectDocumentAiGeneration(Long documentId, String text) {
        try {
            log.info("开始检测文档 AI 生成情况，文档ID: {}", documentId);
            double aiProbability = detectAiGeneratedText(text);
            log.info("文档 AI 生成检测完成，文档ID: {}, 概率: {}", documentId, aiProbability);
            return aiProbability;
        } catch (Exception e) {
            log.error("文档 AI 生成检测失败，文档ID: {}", documentId, e);
            return 0.5;
        }
    }
}