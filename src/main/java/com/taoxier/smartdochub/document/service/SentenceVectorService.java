package com.taoxier.smartdochub.document.service;

import com.alibaba.fastjson.JSON;
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
import java.util.List;
import java.util.Map;

/**
 * 语义向量服务
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SentenceVectorService {

    private final RestTemplate restTemplate;

    private static final String VECTOR_API_URL = "http://localhost:8000/predict/sentence/vector";

    /**
     * 生成文本的语义向量
     * 
     * @param text 文本内容
     * @return 语义向量
     */
    public List<Double> generateVector(String text) {
        int maxRetries = 3;
        int retryCount = 0;
        Exception lastException = null;
        
        while (retryCount < maxRetries) {
            try {
                // 构建请求体
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("text", text);
                requestBody.put("max_length", 1000);

                // 设置请求头
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                // 构建请求实体
                HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

                // 发送请求
                ResponseEntity<String> response = restTemplate.postForEntity(VECTOR_API_URL, requestEntity, String.class);

                // 解析响应
                Map<String, Object> responseBody = JSON.parseObject(response.getBody());
                List<?> rawVector = (List<?>) responseBody.get("vector");

                // 转换为 Double 列表
                List<Double> vector = new java.util.ArrayList<>();
                for (Object item : rawVector) {
                    if (item instanceof Number) {
                        vector.add(((Number) item).doubleValue());
                    } else {
                        throw new BusinessException("向量格式错误");
                    }
                }

                if (retryCount > 0) {
                    log.info("向量生成成功（重试{}次后）", retryCount);
                }
                
                return vector;

            } catch (Exception e) {
                lastException = e;
                retryCount++;
                
                if (retryCount < maxRetries) {
                    log.warn("向量生成失败，正在重试（{}/{}）: {}", retryCount, maxRetries, e.getMessage());
                    try {
                        Thread.sleep(1000 * retryCount); // 递增等待时间
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        
        log.error("向量生成失败，已重试{}次", maxRetries, lastException);
        throw new BusinessException("向量生成失败: " + lastException.getMessage());
    }

    /**
     * 计算两个向量的余弦相似度
     * 
     * @param vector1 向量1
     * @param vector2 向量2
     * @return 相似度（0-1）
     */
    public double calculateCosineSimilarity(List<Double> vector1, List<Double> vector2) {
        if (vector1 == null || vector2 == null || vector1.size() != vector2.size()) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < vector1.size(); i++) {
            dotProduct += vector1.get(i) * vector2.get(i);
            norm1 += Math.pow(vector1.get(i), 2);
            norm2 += Math.pow(vector2.get(i), 2);
        }

        if (norm1 == 0 || norm2 == 0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    /**
     * 计算相似度百分比
     * 
     * @param similarity 余弦相似度
     * @return 相似度百分比（0-100）
     */
    public double calculateSimilarityPercentage(double similarity) {
        return Math.max(0, Math.min(100, similarity * 100));
    }
}