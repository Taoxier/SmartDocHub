package com.taoxier.smartdochub.document.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 文档相似度关联表
 * </p>
 *
 * @author taoxier
 * @since 2025-10-16
 */
@Getter
@Setter
@TableName("doc_similarity")
public class Similarity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 关联ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 源文档ID
     */
    @TableField("source_doc_id")
    private Long sourceDocId;

    /**
     * 目标文档ID
     */
    @TableField("target_doc_id")
    private Long targetDocId;

    /**
     * 相似类型: OVERALL, TEXT, STRUCTURE, TOPIC
     */
    @TableField("similarity_type")
    private String similarityType;

    /**
     * 相似度得分
     */
    @TableField("similarity_score")
    private BigDecimal similarityScore;

    /**
     * 匹配的内容块信息
     */
    @TableField("matched_chunks")
    private String matchedChunks;

    /**
     * 使用的算法
     */
    @TableField("algorithm_used")
    private String algorithmUsed;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;
}
