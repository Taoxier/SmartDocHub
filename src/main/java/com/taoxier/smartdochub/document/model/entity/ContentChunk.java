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
 * 文档内容分块表
 * </p>
 *
 * @author taoxier
 * @since 2025-10-16
 */
@Getter
@Setter
@TableName("doc_content_chunk")
public class ContentChunk implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 内容块ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 文档ID
     */
    @TableField("document_id")
    private Long documentId;

    /**
     * 版本号
     */
    @TableField("version_number")
    private Integer versionNumber;

    /**
     * 块索引
     */
    @TableField("chunk_index")
    private Integer chunkIndex;

    /**
     * 内容类型: TEXT, TABLE, FORMULA, IMAGE, TITLE, ABSTRACT
     */
    @TableField("content_type")
    private String contentType;

    /**
     * 文本内容
     */
    @TableField("content_text")
    private String contentText;

    /**
     * 原始内容（用于表格、公式等）
     */
    @TableField("original_content")
    private String originalContent;

    /**
     * 相似度得分
     */
    @TableField("similarity_score")
    private BigDecimal similarityScore;

    /**
     * AI生成概率
     */
    @TableField("ai_probability")
    private BigDecimal aiProbability;

    /**
     * 匹配的文档信息
     */
    @TableField("matched_documents")
    private String matchedDocuments;

    /**
     * 所在页码
     */
    @TableField("page_number")
    private Integer pageNumber;

    /**
     * 章节标题
     */
    @TableField("section_title")
    private String sectionTitle;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;
}
