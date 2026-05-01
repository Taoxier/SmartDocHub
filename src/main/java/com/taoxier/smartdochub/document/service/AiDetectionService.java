package com.taoxier.smartdochub.document.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.taoxier.smartdochub.common.exception.BusinessException;
import com.taoxier.smartdochub.document.model.entity.AiAnalysisResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    // 模型最大序列长度（512 - 2个特殊token = 510）
    private static final int MAX_SEQUENCE_LENGTH = 510;
    // 每次截取的文本长度（字符数）
    // 中文约1字符=1token，英文约1字符=0.25token，混合文本取保守值
    // 200字符约产生120-240token，加上2个特殊token后不超过512
    private static final int CHUNK_SIZE = 200;

    /**
     * 检测文本是否由 AI 生成
     * 
     * @param text 文本内容
     * @return AI 分析结果
     */
    public AiAnalysisResult detectAiGeneratedText(String text) {
        try {
            // 如果文本长度超过限制，分块处理
            if (text.length() > CHUNK_SIZE) {
                return detectLongTextAiGeneration(text);
            }

            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("text", text);
            requestBody.put("max_length", MAX_SEQUENCE_LENGTH);
            requestBody.put("truncation", true);

            log.info("AI检测请求 - 文本字符长度: {}", text.length());

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 构建请求实体
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            // 发送请求
            ResponseEntity<String> response = restTemplate.postForEntity(AI_DETECTION_URL, requestEntity, String.class);
            log.info("AI检测响应状态码: {}", response.getStatusCode());
            log.info("AI检测响应体: {}", response.getBody());

            // 解析响应
            JSONObject responseBody = JSON.parseObject(response.getBody());

            // 构建AI分析结果
            AiAnalysisResult result = new AiAnalysisResult();
            result.setAiProbability(BigDecimal.valueOf(responseBody.getDouble("ai_probability")));
            result.setConfidence(BigDecimal.valueOf(responseBody.getDouble("confidence")));
            result.setDetectedModel(responseBody.getString("detected_model"));

            // 处理key_features
            JSONArray keyFeatures = responseBody.getJSONArray("key_features");
            if (keyFeatures != null && !keyFeatures.isEmpty()) {
                result.setKeyFeatures(keyFeatures.toJSONString());
                log.info("key_features解析成功: {}", result.getKeyFeatures());
            } else {
                log.warn("key_features为空或不存在");
            }

            // 设置分析结果
            result.setResult(responseBody.getString("label"));

            return result;

        } catch (Exception e) {
            log.error("AI 检测失败: {}", e.getMessage(), e);
            // 发生错误时返回默认值
            AiAnalysisResult result = new AiAnalysisResult();
            result.setAiProbability(BigDecimal.valueOf(0.5));
            result.setConfidence(BigDecimal.valueOf(0.5));
            result.setResult("PASS");
            return result;
        }
    }

    /**
     * 检测长文本是否由 AI 生成
     * 
     * @param text 长文本内容
     * @return AI 分析结果
     */
    private AiAnalysisResult detectLongTextAiGeneration(String text) {
        log.info("检测长文本，长度: {}", text.length());

        List<Double> aiProbabilities = new ArrayList<>();
        List<Double> confidences = new ArrayList<>();
        JSONArray allKeyFeatures = new JSONArray();
        String detectedModel = null;

        // 分块处理
        for (int i = 0; i < text.length(); i += CHUNK_SIZE) {
            int end = Math.min(i + CHUNK_SIZE, text.length());
            String chunk = text.substring(i, end);

            try {
                // 构建请求体
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("text", chunk);
                requestBody.put("max_length", MAX_SEQUENCE_LENGTH);
                requestBody.put("truncation", true);

                log.info("AI检测分块请求 - 分块字符长度: {}, 文本起始位置: {}", chunk.length(), i);

                // 设置请求头
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                // 构建请求实体
                HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

                // 发送请求
                ResponseEntity<String> response = restTemplate.postForEntity(AI_DETECTION_URL, requestEntity,
                        String.class);
                log.info("AI检测分块响应状态码: {}", response.getStatusCode());

                // 解析响应
                JSONObject responseBody = JSON.parseObject(response.getBody());

                // 收集结果
                aiProbabilities.add(responseBody.getDouble("ai_probability"));
                confidences.add(responseBody.getDouble("confidence"));

                if (detectedModel == null) {
                    detectedModel = responseBody.getString("detected_model");
                }

                // 收集key_features
                JSONArray keyFeatures = responseBody.getJSONArray("key_features");
                if (keyFeatures != null && !keyFeatures.isEmpty()) {
                    allKeyFeatures.addAll(keyFeatures);
                }

                log.info("当前分块ai率：{}，可信度：{}，key_features：{}",responseBody.getDouble("ai_probability"),responseBody.getDouble("confidence"),keyFeatures);

            } catch (Exception e) {
                log.error("分块AI检测失败: {}", e.getMessage());
                // 发生错误时添加默认值
                aiProbabilities.add(0.5);
                confidences.add(0.5);
            }
        }

        // 计算平均结果
        double avgAiProbability = aiProbabilities.stream().mapToDouble(Double::doubleValue).average().orElse(0.5);
        double avgConfidence = confidences.stream().mapToDouble(Double::doubleValue).average().orElse(0.5);

        // 构建最终结果
        AiAnalysisResult result = new AiAnalysisResult();
        result.setAiProbability(BigDecimal.valueOf(avgAiProbability));
        result.setConfidence(BigDecimal.valueOf(avgConfidence));
        result.setDetectedModel(detectedModel);

        // 处理key_features（取前几个最有代表性的）
        if (!allKeyFeatures.isEmpty()) {
            // 取前两个特征（语义连贯性分析和句式多样性评估）
            JSONArray selectedFeatures = new JSONArray();
            for (int i = 0; i < Math.min(2, allKeyFeatures.size()); i++) {
                selectedFeatures.add(allKeyFeatures.get(i));
            }
            result.setKeyFeatures(selectedFeatures.toJSONString());
            log.info("长文本key_features处理成功: {}", result.getKeyFeatures());
        } else {
            log.warn("长文本key_features为空");
        }

        // 设置分析结果
        result.setResult(avgAiProbability > 0.5 ? "AI" : "Human");

        log.info("长文本AI检测完成，平均概率: {}, 平均置信度: {}", avgAiProbability, avgConfidence);
        return result;
    }

    /**
     * 检测文档是否由 AI 生成
     * 
     * @param documentId 文档ID
     * @param text       文档文本内容
     * @return AI 分析结果
     */
    public AiAnalysisResult detectDocumentAiGeneration(Long documentId, String text) {
        try {
            log.info("开始检测文档 AI 生成情况，文档ID: {}", documentId);
            AiAnalysisResult result = detectAiGeneratedText(text);
            result.setDocumentId(documentId);
            result.setAnalysisTime(java.time.LocalDateTime.now());
            result.setUpdateTime(java.time.LocalDateTime.now());
            log.info("文档 AI 生成检测完成，文档ID: {}, 概率: {}", documentId, result.getAiProbability());
            return result;
        } catch (Exception e) {
            log.error("文档 AI 生成检测失败，文档ID: {}", documentId, e);
            // 发生错误时返回默认值
            AiAnalysisResult result = new AiAnalysisResult();
            result.setDocumentId(documentId);
            result.setAnalysisTime(java.time.LocalDateTime.now());
            result.setUpdateTime(java.time.LocalDateTime.now());
            result.setAiProbability(BigDecimal.valueOf(0.5));
            result.setConfidence(BigDecimal.valueOf(0.5));
            result.setResult("PASS");
            return result;
        }
    }
}