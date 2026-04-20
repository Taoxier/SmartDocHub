package com.taoxier.smartdochub.document.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "文档详情VO")
public class DocumentDetailVO {

    @Schema(description = "文档ID")
    private Long id;

    @Schema(description = "文档标题")
    private String title;

    @Schema(description = "文档描述")
    private String description;

    @Schema(description = "文档分类")
    private String category;

    @Schema(description = "原始文件名")
    private String originalFilename;

    @Schema(description = "存储路径")
    private String storagePath;

    @Schema(description = "预览URL")
    private String previewUrl;

    @Schema(description = "文件类型")
    private String fileType;

    @Schema(description = "文件大小(字节)")
    private Long fileSize;

    @Schema(description = "上传用户ID")
    private Long uploadUserId;

    @Schema(description = "上传用户名")
    private String uploadUserName;

    @Schema(description = "处理状态")
    private String processStatus;

    @Schema(description = "处理进度")
    private Integer processProgress;

    @Schema(description = "处理信息")
    private String processMessage;

    @Schema(description = "页数")
    private Integer pageCount;

    @Schema(description = "字数")
    private Integer wordCount;

    @Schema(description = "字符数")
    private Integer characterCount;

    @Schema(description = "解析后的文本内容")
    private String parsedContent;

    @Schema(description = "总体重复率")
    private BigDecimal overallSimilarity;

    @Schema(description = "文字重复率")
    private BigDecimal textSimilarity;

    @Schema(description = "表格重复率")
    private BigDecimal tableSimilarity;

    @Schema(description = "公式重复率")
    private BigDecimal formulaSimilarity;

    @Schema(description = "AI生成概率")
    private BigDecimal aiProbability;

    @Schema(description = "检测到的AI模型")
    private String detectedAiModel;

    @Schema(description = "质量评分")
    private BigDecimal qualityScore;

    @Schema(description = "可读性评分")
    private BigDecimal readabilityScore;

    @Schema(description = "是否重复")
    private Byte isDuplicate;

    @Schema(description = "是否AI生成")
    private Byte isAiGenerated;

    @Schema(description = "是否公开")
    private Byte isPublic;

    @Schema(description = "需要审核")
    private Byte needsReview;

    @Schema(description = "浏览次数")
    private Integer viewCount;

    @Schema(description = "下载次数")
    private Integer downloadCount;

    @Schema(description = "收藏次数")
    private Integer favoriteCount;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "内容分块列表")
    private List<ContentChunkVO> chunks;

    @Schema(description = "文档关键词")
    private List<TopicVO> topics;

    @Data
    @Schema(description = "内容块VO")
    public static class ContentChunkVO {
        @Schema(description = "块ID")
        private Long id;

        @Schema(description = "块索引")
        private Integer chunkIndex;

        @Schema(description = "内容类型")
        private String contentType;

        @Schema(description = "文本内容")
        private String contentText;

        @Schema(description = "原始内容")
        private String originalContent;

        @Schema(description = "相似度得分")
        private BigDecimal similarityScore;

        @Schema(description = "AI生成概率")
        private BigDecimal aiProbability;

        @Schema(description = "页码")
        private Integer pageNumber;

        @Schema(description = "章节标题")
        private String sectionTitle;
    }

    @Data
    @Schema(description = "主题VO")
    public static class TopicVO {
        @Schema(description = "主题ID")
        private Long id;

        @Schema(description = "主题类型")
        private String topicType;

        @Schema(description = "主题值")
        private String topicValue;

        @Schema(description = "置信度")
        private BigDecimal confidence;

        @Schema(description = "权重")
        private BigDecimal weight;
    }
}
