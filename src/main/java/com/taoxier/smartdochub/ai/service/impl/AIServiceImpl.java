package com.taoxier.smartdochub.ai.service.impl;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.ciModel.auditing.DocumentAuditingRequest;
import com.qcloud.cos.model.ciModel.auditing.DocumentAuditingResponse;
import com.qcloud.cos.model.ciModel.auditing.DocumentAuditingJobsDetail;
import com.qcloud.cos.model.ciModel.auditing.DocumentResultInfo;
import com.qcloud.cos.model.ciModel.auditing.DocumentInputObject;
import com.qcloud.cos.model.ciModel.auditing.Conf;
import com.qcloud.cos.region.Region;
import com.taoxier.smartdochub.ai.service.AIService;
import com.taoxier.smartdochub.comment.model.vo.CommentAuditResultVO;
import com.taoxier.smartdochub.config.properties.BaiduAiProperties;
import com.taoxier.smartdochub.config.properties.CozeProperties;
import com.taoxier.smartdochub.config.properties.DeepSeekProperties;
import com.taoxier.smartdochub.config.properties.TencentCloudProperties;
import com.taoxier.smartdochub.document.model.entity.DocCategory;
import com.taoxier.smartdochub.document.service.DocCategoryService;
import com.taoxier.smartdochub.comment.service.SensitiveWordService;
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
    private final TencentCloudProperties tencentCloudProperties;
    private final CozeProperties cozeProperties;
    private final DocCategoryService docCategoryService;
    private final SensitiveWordService sensitiveWordService;
    private final RestTemplate restTemplate;

    @Override
    public String classifyDocument(String content) {
        // 从数据库获取所有分类
        List<DocCategory> categoryList = docCategoryService.getAllCategories();
        if (categoryList.isEmpty()) {
            return "其他文档";
        }

        // 构建分类字符串
        StringBuilder categoriesBuilder = new StringBuilder();
        for (DocCategory category : categoryList) {
            categoriesBuilder.append(category.getName()).append("、");
        }
        // 移除最后一个分隔符
        String categories = categoriesBuilder.toString();
        if (categories.endsWith("、")) {
            categories = categories.substring(0, categories.length() - 1);
        }

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

    @Override
    public String auditComment(String content) {
        try {
            // 使用敏感词检测服务进行评论审核
            Map<String, Object> detectionResult = sensitiveWordService.detectSensitiveWords(content);
            String auditStatus = sensitiveWordService.getAuditStatus(detectionResult);

            log.info("评论审核结果: {}, 敏感词检测结果: {}", auditStatus, detectionResult);

            return auditStatus;
        } catch (Exception e) {
            log.error("审核评论失败: {}", e.getMessage());
            return "APPROVED"; // 默认通过
        }
    }

    @Override
    public CommentAuditResultVO auditCommentDetail(String content) {
        CommentAuditResultVO resultVO = new CommentAuditResultVO();

        try {
            // 1. 敏感词检测
            CommentAuditResultVO.SensitiveWordResult sensitiveResult = new CommentAuditResultVO.SensitiveWordResult();
            Map<String, Object> detectionResult = sensitiveWordService.detectSensitiveWords(content);

            sensitiveResult.setHasSensitive((Boolean) detectionResult.get("hasSensitive"));
            sensitiveResult.setCategories((List<String>) detectionResult.get("categories"));
            sensitiveResult.setWords((List<String>) detectionResult.get("words"));
            sensitiveResult.setDetails((Map<String, List<String>>) detectionResult.get("details"));

            // 获取敏感词审核状态
            String sensitiveStatus = sensitiveWordService.getAuditStatus(detectionResult);
            if ("APPROVED".equals(sensitiveStatus)) {
                sensitiveResult.setCheckStatus("PASS");
            } else if ("REJECTED".equals(sensitiveStatus)) {
                sensitiveResult.setCheckStatus("REJECT");
            } else {
                sensitiveResult.setCheckStatus("WARN");
            }
            resultVO.setSensitiveWordResult(sensitiveResult);

            // 2. AI情感分析（调用Python大模型服务）
            CommentAuditResultVO.SentimentResult sentimentResult = new CommentAuditResultVO.SentimentResult();
            try {
                Map<String, Object> aiAuditResult = callPythonAIAuditService(content);
                if (aiAuditResult != null) {
                    String sentimentStatus = (String) aiAuditResult.get("status");
                    Map<String, Object> sentimentAnalysis = (Map<String, Object>) aiAuditResult
                            .get("sentiment_analysis");

                    if (sentimentAnalysis != null) {
                        String sentiment = (String) sentimentAnalysis.get("sentiment");
                        Double score = ((Number) sentimentAnalysis.get("score")).doubleValue();

                        sentimentResult.setSentiment(sentiment);
                        sentimentResult.setConfidence(score);
                        sentimentResult.setSentimentScore(sentiment.equals("POSITIVE") ? score : -score);
                    }

                    if ("REJECT".equals(sentimentStatus)) {
                        sentimentResult.setCheckStatus("REJECT");
                    } else if ("MANUAL_REVIEW".equals(sentimentStatus)) {
                        sentimentResult.setCheckStatus("WARN");
                    } else {
                        sentimentResult.setCheckStatus("PASS");
                    }

                    List<String> emotionTags = new ArrayList<>();
                    if (sentimentAnalysis != null) {
                        String sentiment = (String) sentimentAnalysis.get("sentiment");
                        emotionTags.add(sentiment);
                    }
                    sentimentResult.setEmotionTags(emotionTags);
                } else {
                    // 默认值
                    sentimentResult.setSentiment("NEUTRAL");
                    sentimentResult.setConfidence(0.0);
                    sentimentResult.setSentimentScore(0.0);
                    sentimentResult.setCheckStatus("PASS");
                    sentimentResult.setEmotionTags(new ArrayList<>());
                }
            } catch (Exception e) {
                log.error("调用Python AI审核服务失败: {}", e.getMessage());
                // 默认值
                sentimentResult.setSentiment("NEUTRAL");
                sentimentResult.setConfidence(0.0);
                sentimentResult.setSentimentScore(0.0);
                sentimentResult.setCheckStatus("PASS");
                sentimentResult.setEmotionTags(new ArrayList<>());
            }
            resultVO.setSentimentResult(sentimentResult);

            // 3. 综合判断审核状态
            String finalStatus = determineFinalAuditStatus(sensitiveResult, sentimentResult);
            resultVO.setAuditStatus(finalStatus);

            // 4. 生成审核建议
            resultVO.setSuggestion(generateAuditSuggestion(sensitiveResult, sentimentResult));
            resultVO.setAuditTime(java.time.LocalDateTime.now().toString());

            log.info("评论详细审核完成 - 敏感词检测: {}, AI情感分析: {}, 综合状态: {}",
                    sensitiveResult.getCheckStatus(), sentimentResult.getCheckStatus(), finalStatus);

            return resultVO;

        } catch (Exception e) {
            log.error("详细审核评论失败: {}", e.getMessage());
            // 返回默认通过结果
            resultVO.setAuditStatus("APPROVED");
            resultVO.setSuggestion("审核服务异常，默认通过");
            resultVO.setAuditTime(java.time.LocalDateTime.now().toString());
            return resultVO;
        }
    }

    /**
     * 调用Python AI审核服务
     */
    private Map<String, Object> callPythonAIAuditService(String content) {
        try {
            String url = "http://localhost:8000/predict/comment/audit/advanced";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("text", content);
            requestBody.put("max_length", 1000);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            return response.getBody();
        } catch (Exception e) {
            log.error("调用Python AI审核服务失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 综合判断最终审核状态
     */
    private String determineFinalAuditStatus(CommentAuditResultVO.SensitiveWordResult sensitiveResult,
            CommentAuditResultVO.SentimentResult sentimentResult) {
        // 敏感词检测为拒绝，直接拒绝
        if ("REJECT".equals(sensitiveResult.getCheckStatus())) {
            return "REJECTED";
        }

        // 敏感词检测为警告，需要人工审核
        if ("WARN".equals(sensitiveResult.getCheckStatus())) {
            return "MANUAL_REVIEW";
        }

        // AI情感分析为异常，可能需要人工审核
        if ("WARN".equals(sentimentResult.getCheckStatus())) {
            return "MANUAL_REVIEW";
        }

        // 全部通过
        return "APPROVED";
    }

    /**
     * 生成审核建议
     */
    private String generateAuditSuggestion(CommentAuditResultVO.SensitiveWordResult sensitiveResult,
            CommentAuditResultVO.SentimentResult sentimentResult) {
        if ("REJECT".equals(sensitiveResult.getCheckStatus())) {
            return "评论包含违规内容，已被系统自动拒绝";
        }

        if ("WARN".equals(sensitiveResult.getCheckStatus())) {
            return "评论可能包含敏感内容，建议人工复核";
        }

        if ("WARN".equals(sentimentResult.getCheckStatus())) {
            return "评论情感分析异常，建议人工复核";
        }

        return "评论审核通过";
    }

    @Override
    public String auditDocument(Long documentId, String auditType, String storagePath) {
        try {
            log.info("========== AIServiceImpl.auditDocument 开始 ==========");
            log.info("文档ID: {}, 审核类型: {}, 存储路径: {}", documentId, auditType, storagePath);
            
            // 从存储路径中提取 COS Key
            String cosKey = extractCosKey(storagePath);
            log.info("提取的 COS Key: {}", cosKey);
            
            COSCredentials credentials = new BasicCOSCredentials(
                    tencentCloudProperties.getSecretId(),
                    tencentCloudProperties.getSecretKey());
            ClientConfig clientConfig = new ClientConfig(new Region(tencentCloudProperties.getRegion()));
            COSClient cosClient = new COSClient(credentials, clientConfig);
            log.info("COS客户端创建成功");

            String bucketName = tencentCloudProperties.getBucketName();
            log.info("存储桶名称: {}", bucketName);

            DocumentAuditingRequest request = new DocumentAuditingRequest();
            request.setBucketName(bucketName);

            DocumentInputObject input = new DocumentInputObject();
            input.setObject(cosKey);
            request.setInput(input);

            Conf conf = new Conf();
            conf.setDetectType("Porn,Ads,Illegal,Abuse");
            request.setConf(conf);
            log.info("审核请求准备完成");

            DocumentAuditingResponse response = cosClient.createAuditingDocumentJobs(request);
            String jobId = response.getJobsDetail().getJobId();
            log.info("审核任务提交成功，JobId: {}", jobId);

            log.info("提交腾讯云文档审核任务成功，文档ID: {}, JobId: {}, COS Key: {}", documentId, jobId, cosKey);

            String auditResult = pollAuditResult(cosClient, bucketName, jobId);
            log.info("审核轮询完成，结果: {}", auditResult);

            cosClient.shutdown();

            log.info("========== AIServiceImpl.auditDocument 结束 ==========");
            return auditResult;
        } catch (Exception e) {
            log.error("审核文档失败", e);
            log.error("异常信息: {}", e.getMessage());
            return "{\"status\": \"REJECT\", \"message\": \"审核失败\"}";
        }
    }
    
    /**
     * 从存储路径 URL 中提取 COS Key
     */
    private String extractCosKey(String storagePath) {
        try {
            String customDomain = tencentCloudProperties.getCustomDomain();
            String bucketName = tencentCloudProperties.getBucketName();
            String region = tencentCloudProperties.getRegion();
            
            // 优先使用自定义域名
            if (customDomain != null && !customDomain.isEmpty() && storagePath.startsWith(customDomain)) {
                log.info("使用自定义域名模式, customDomain: {}", customDomain);
                String key = storagePath.substring(customDomain.length() + 1);
                log.info("提取的key: {}", key);
                return key;
            } else {
                // 从COS默认URL中提取文件键
                // 格式: https://bucketName.region.myqcloud.com/key
                String regionSuffix = "." + region + ".myqcloud.com/";
                int regionIdx = storagePath.indexOf(regionSuffix);
                if (regionIdx > 0) {
                    // 提取key
                    String key = storagePath.substring(regionIdx + regionSuffix.length() - 1);
                    if (key.startsWith("/")) {
                        key = key.substring(1);
                    }
                    log.info("使用默认域名模式, key: {}", key);
                    return key;
                } else {
                    // 如果不是URL格式，直接返回
                    log.info("不是URL格式，直接返回: {}", storagePath);
                    return storagePath;
                }
            }
        } catch (Exception e) {
            log.error("提取COS Key失败", e);
            return storagePath;
        }
    }

    private String pollAuditResult(COSClient cosClient, String bucketName, String jobId) {
        int maxRetry = 40;
        int interval = 3000;

        for (int i = 0; i < maxRetry; i++) {
            DocumentAuditingRequest queryReq = new DocumentAuditingRequest();
            queryReq.setBucketName(bucketName);
            queryReq.setJobId(jobId);

            DocumentAuditingResponse response = cosClient.describeAuditingDocumentJob(queryReq);
            DocumentAuditingJobsDetail detail = response.getJobsDetail();
            String state = detail.getState();

            log.info("第{}次查询审核状态: {}, JobId: {}", i + 1, state, jobId);

            if ("Success".equals(state)) {
                return parseAuditResult(detail);
            } else if ("Failed".equals(state)) {
                return "{\"status\": \"REJECT\", \"message\": \"审核失败\"}";
            } else {
                try {
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return "{\"status\": \"REJECT\", \"message\": \"轮询被中断\"}";
                }
            }
        }

        return "{\"status\": \"WARN\", \"message\": \"审核超时，请稍后手动查询\"}";
    }

    private String parseAuditResult(DocumentAuditingJobsDetail detail) {
        String suggestion = detail.getSuggestion();
        String label = detail.getLabel();
        boolean hasViolation = false;
        StringBuilder violations = new StringBuilder();

        if ("1".equals(suggestion) || "2".equals(suggestion)) {
            hasViolation = true;
            violations.append("整体建议: ").append("1".equals(suggestion) ? "违规" : "疑似");
            if (label != null) {
                violations.append(", 违规类型: ").append(label);
            }
        }

        try {
            DocumentResultInfo labels = detail.getLabels();
            if (labels != null) {
                if (labels.getPornInfo() != null && "1".equals(labels.getPornInfo().getHitFlag())) {
                    violations.append("; 色情内容: ").append(labels.getPornInfo().getScore()).append("分");
                }
                if (labels.getAdsInfo() != null && "1".equals(labels.getAdsInfo().getHitFlag())) {
                    violations.append("; 广告内容: ").append(labels.getAdsInfo().getScore()).append("分");
                }
                if (labels.getPoliticsInfo() != null && "1".equals(labels.getPoliticsInfo().getHitFlag())) {
                    violations.append("; 政治内容: ").append(labels.getPoliticsInfo().getScore()).append("分");
                }
                if (labels.getTerroristInfo() != null && "1".equals(labels.getTerroristInfo().getHitFlag())) {
                    violations.append("; 暴恐内容: ").append(labels.getTerroristInfo().getScore()).append("分");
                }
            }
        } catch (Exception e) {
            log.warn("获取详细审核分类信息失败: {}", e.getMessage());
        }

        if (hasViolation) {
            return "{\"status\": \"REJECT\", \"message\": \"发现违规内容: " + violations.toString() + "\"}";
        } else {
            return "{\"status\": \"PASS\", \"message\": \"审核通过\"}";
        }
    }

    @Override
    public String translateDocument(Long documentId, String sourceLanguage, String targetLanguage) {
        try {
            // 调用Coze API进行文档翻译
            String apiKey = cozeProperties.getApiKey();
            String apiUrl = cozeProperties.getApiUrl();

            if (apiKey == null) {
                throw new RuntimeException("Coze API密钥未配置");
            }

            // 构建Coze API请求
            // 参考文档：https://docs.coze.cn/docs/developer_guides/api_call_guide

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-4o");
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 2048);

            List<Map<String, Object>> messages = new ArrayList<>();
            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");
            message.put("content",
                    "请将以下文档内容从" + sourceLanguage + "翻译为" + targetLanguage + "：\n文档ID: " + documentId + "\n文档内容：[文档内容]");
            messages.add(message);

            requestBody.put("messages", messages);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, requestEntity, String.class);

            log.info("调用Coze API翻译文档，文档ID: {}, 响应: {}", documentId, response.getBody());

            // 处理响应结果，保存翻译后的内容
            // 这里需要根据实际的响应格式进行解析

            return "/translations/doc_" + documentId + "_" + targetLanguage + ".txt";
        } catch (Exception e) {
            log.error("翻译文档失败: {}", e.getMessage());
            throw new RuntimeException("翻译失败: " + e.getMessage());
        }
    }

    @Override
    public String convertDocument(Long documentId, String sourceFormat, String targetFormat) {
        try {
            // 调用Coze API进行文档格式转换
            String apiKey = cozeProperties.getApiKey();
            String apiUrl = cozeProperties.getApiUrl();

            if (apiKey == null) {
                throw new RuntimeException("Coze API密钥未配置");
            }

            // 构建Coze API请求
            // 参考文档：https://docs.coze.cn/docs/developer_guides/api_call_guide

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-4o");
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 2048);

            List<Map<String, Object>> messages = new ArrayList<>();
            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", "请将文档ID为" + documentId + "的文档从" + sourceFormat + "格式转换为" + targetFormat + "格式。");
            messages.add(message);

            requestBody.put("messages", messages);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, requestEntity, String.class);

            log.info("调用Coze API转换文档格式，文档ID: {}, 响应: {}", documentId, response.getBody());

            // 处理响应结果，保存转换后的文件
            // 这里需要根据实际的响应格式进行解析

            return "/conversions/doc_" + documentId + "." + targetFormat;
        } catch (Exception e) {
            log.error("转换文档格式失败: {}", e.getMessage());
            throw new RuntimeException("格式转换失败: " + e.getMessage());
        }
    }
}
