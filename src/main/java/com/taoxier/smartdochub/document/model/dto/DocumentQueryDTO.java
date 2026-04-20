package com.taoxier.smartdochub.document.model.dto;

import com.taoxier.smartdochub.common.base.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "文档查询条件")
public class DocumentQueryDTO extends BasePageQuery {

    @Schema(description = "文档标题(模糊搜索)")
    private String title;

    @Schema(description = "文件类型")
    private String fileType;

    @Schema(description = "上传用户ID")
    private Long uploadUserId;

    @Schema(description = "是否公开")
    private Byte isPublic;

    @Schema(description = "是否重复")
    private Byte isDuplicate;

    @Schema(description = "是否AI生成")
    private Byte isAiGenerated;

    @Schema(description = "处理状态")
    private String processStatus;

    @Schema(description = "关键词搜索(标题、描述、内容)")
    private String keyword;

    @Schema(description = "分类")
    private String category;

    @Schema(description = "标签(多个逗号分隔)")
    private String tags;

    @Schema(description = "排序字段(createTime, viewCount, downloadCount)")
    private String sortBy;

    @Schema(description = "排序方向(asc/desc)")
    private String sortOrder;
}
