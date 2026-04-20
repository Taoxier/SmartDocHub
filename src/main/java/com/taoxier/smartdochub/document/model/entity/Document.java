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
 * 文档主表
 * </p>
 *
 * @author taoxier
 * @since 2025-10-16
 */
@Getter
@Setter
@TableName("doc_document")
public class Document implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 文档ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 文档标题
     */
    @TableField("title")
    private String title;

    /**
     * 文档描述
     */
    @TableField("description")
    private String description;

    /**
     * 文档分类
     */
    @TableField("category")
    private String category;

    /**
     * 原始文件名
     */
    @TableField("original_filename")
    private String originalFilename;

    /**
     * 存储路径
     */
    @TableField("storage_path")
    private String storagePath;

    /**
     * 文件大小(字节)
     */
    @TableField("file_size")
    private Long fileSize;

    /**
     * 文件类型
     */
    @TableField("file_type")
    private String fileType;

    /**
     * 文件哈希值，用于去重
     */
    @TableField("file_hash")
    private String fileHash;

    /**
     * 上传用户ID
     */
    @TableField("upload_user_id")
    private Long uploadUserId;

    /**
     * 处理状态: UPLOADED, PARSING, SIMILARITY_CHECKING, AI_DETECTING, COMPLETED, FAILED
     */
    @TableField("process_status")
    private String processStatus;

    /**
     * 处理进度(0-100)
     */
    @TableField("process_progress")
    private Integer processProgress;

    /**
     * 处理状态信息
     */
    @TableField("process_message")
    private String processMessage;

    /**
     * 总页数
     */
    @TableField("page_count")
    private Integer pageCount;

    /**
     * 总字数
     */
    @TableField("word_count")
    private Integer wordCount;

    /**
     * 总字符数
     */
    @TableField("character_count")
    private Integer characterCount;

    /**
     * 解析后的完整文本（用于搜索）
     */
    @TableField("parsed_content")
    private String parsedContent;

    /**
     * 总体重复率
     */
    @TableField("overall_similarity")
    private BigDecimal overallSimilarity;

    /**
     * 文字重复率
     */
    @TableField("text_similarity")
    private BigDecimal textSimilarity;

    /**
     * 表格重复率
     */
    @TableField("table_similarity")
    private BigDecimal tableSimilarity;

    /**
     * 公式重复率
     */
    @TableField("formula_similarity")
    private BigDecimal formulaSimilarity;

    /**
     * AI生成总体概率
     */
    @TableField("ai_probability")
    private BigDecimal aiProbability;

    /**
     * 检测到的AI模型类型
     */
    @TableField("detected_ai_model")
    private String detectedAiModel;

    /**
     * 质量评分
     */
    @TableField("quality_score")
    private BigDecimal qualityScore;

    /**
     * 可读性评分
     */
    @TableField("readability_score")
    private BigDecimal readabilityScore;

    /**
     * 是否重复
     */
    @TableField("is_duplicate")
    private Byte isDuplicate;

    /**
     * 是否AI生成
     */
    @TableField("is_ai_generated")
    private Byte isAiGenerated;

    /**
     * 是否公开
     */
    @TableField("is_public")
    private Byte isPublic;

    /**
     * 需要人工审核
     */
    @TableField("needs_review")
    private Byte needsReview;

    /**
     * 浏览次数
     */
    @TableField("view_count")
    private Integer viewCount;

    /**
     * 下载次数
     */
    @TableField("download_count")
    private Integer downloadCount;

    /**
     * 收藏次数
     */
    @TableField("favorite_count")
    private Integer favoriteCount;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;

    /**
     * 逻辑删除
     */
    @TableField("is_deleted")
    private Byte isDeleted;

    /**
     * 当前版本号
     */
    @TableField("current_version")
    private Integer currentVersion;

    /**
     * 版本总数
     */
    @TableField("version_count")
    private Integer versionCount;
}
