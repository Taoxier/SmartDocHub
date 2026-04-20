package com.taoxier.smartdochub.document.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

@Data
@Schema(description = "文档更新DTO")
public class DocumentUpdateDTO {

    @Schema(description = "文档ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "文档标题")
    @Size(max = 500, message = "标题长度不能超过500字符")
    private String title;

    @Schema(description = "文档描述")
    private String description;

    @Schema(description = "是否公开(0-私有,1-公开)")
    private Byte isPublic;

    @Schema(description = "文档分类")
    private String category;

    @Schema(description = "文档标签")
    private List<String> tags;
}
