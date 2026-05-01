package com.taoxier.smartdochub.document.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI分析结果表
 */
@Getter
@Setter
@TableName("doc_ai_analysis")
public class AiAnalysisResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 分析ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 文档ID
     */
    @TableField("document_id")
    private Long documentId;

    /**
     * AI生成概率（0-100）
     */
    @TableField("ai_probability")
    private BigDecimal aiProbability;

    /**
     * 置信度（0-100）
     */
    @TableField("confidence")
    private BigDecimal confidence;

    /**
     * 检测到的AI模型类型
     */
    @TableField("detected_model")
    private String detectedModel;

    /**
     * 关键特征段落（JSON格式）
     */
    @TableField("key_features")
    private String keyFeatures;

    /**
     * 分析结果（PASS, WARN, REJECT）
     */
    @TableField("result")
    private String result;

    /**
     * 分析时间
     */
    @TableField("analysis_time")
    private LocalDateTime analysisTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;
}