package com.taoxier.smartdochub.document.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import com.taoxier.smartdochub.document.model.entity.Topic;

@Data
@Schema(description = "文档列表VO")
public class DocumentVO {

    @Schema(description = "文档ID")
    private Long id;

    @Schema(description = "文档标题")
    private String title;

    @Schema(description = "文档描述")
    private String description;

    @Schema(description = "原始文件名")
    private String originalFilename;

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

    @Schema(description = "页数")
    private Integer pageCount;

    @Schema(description = "字数")
    private Integer wordCount;

    @Schema(description = "总体重复率")
    private BigDecimal overallSimilarity;

    @Schema(description = "AI生成概率")
    private BigDecimal aiProbability;

    @Schema(description = "是否重复")
    private Byte isDuplicate;

    @Schema(description = "是否AI生成")
    private Byte isAiGenerated;

    @Schema(description = "是否公开")
    private Byte isPublic;

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

    @Schema(description = "文档分类")
    private String category;

    @Schema(description = "文档标签")
    private List<Topic> topics;
}
