package com.taoxier.smartdochub.comment.model.vo;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 评论审核结果VO
 * 包含敏感词检测和AI情感分析两部分结果
 */
@Data
public class CommentAuditResultVO {

    /**
     * 综合审核状态
     * APPROVED - 通过
     * MANUAL_REVIEW - 需要人工审核
     * REJECTED - 拒绝
     */
    private String auditStatus;

    /**
     * 敏感词检测结果
     */
    private SensitiveWordResult sensitiveWordResult;

    /**
     * AI情感分析结果（后续接入）
     */
    private SentimentResult sentimentResult;

    /**
     * 审核时间
     */
    private String auditTime;

    /**
     * 审核建议
     */
    private String suggestion;

    /**
     * 敏感词检测结果
     */
    @Data
    public static class SensitiveWordResult {
        /**
         * 是否包含敏感词
         */
        private Boolean hasSensitive;

        /**
         * 敏感词类别列表
         */
        private List<String> categories;

        /**
         * 敏感词列表
         */
        private List<String> words;

        /**
         * 敏感词详情（按类别分组）
         */
        private Map<String, List<String>> details;

        /**
         * 敏感词检测状态
         * PASS - 无敏感词
         * WARN - 包含警告级敏感词
         * REJECT - 包含拒绝级敏感词
         */
        private String checkStatus;
    }

    /**
     * AI情感分析结果（后续接入大模型）
     */
    @Data
    public static class SentimentResult {
        /**
         * 情感倾向
         * POSITIVE - 正面
         * NEUTRAL - 中性
         * NEGATIVE - 负面
         */
        private String sentiment;

        /**
         * 情感置信度 (0-1)
         */
        private Double confidence;

        /**
         * 情感得分 (-1到1, 负数表示负面, 正数表示正面)
         */
        private Double sentimentScore;

        /**
         * 情感分析状态
         * PASS - 正常
         * WARN - 异常情感
         */
        private String checkStatus;

        /**
         * 详细情感标签
         */
        private List<String> emotionTags;
    }
}