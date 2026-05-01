package com.taoxier.smartdochub.document.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "版本差异对比结果")
public class VersionDiffVO {

    @Schema(description = "版本1号")
    private Integer version1;

    @Schema(description = "版本2号")
    private Integer version2;

    @Schema(description = "版本1的内容块列表")
    private List<DiffChunk> chunksA;

    @Schema(description = "版本2的内容块列表")
    private List<DiffChunk> chunksB;

    @Schema(description = "差异统计")
    private DiffStats stats;

    @Data
    @Schema(description = "差异块")
    public static class DiffChunk {
        @Schema(description = "行号")
        private Integer lineNumber;

        @Schema(description = "内容")
        private String content;

        @Schema(description = "差异类型: ADDED(新增), DELETED(删除), MODIFIED(修改), UNCHANGED(未变)")
        private String diffType;

        @Schema(description = "对应的版本1行号（用于对齐显示）")
        private Integer counterpartLineNumber;
    }

    @Schema(description = "差异统计")
    @Data
    public static class DiffStats {
        @Schema(description = "新增行数")
        private Integer addedCount;

        @Schema(description = "删除行数")
        private Integer deletedCount;

        @Schema(description = "修改行数")
        private Integer modifiedCount;

        @Schema(description = "未变行数")
        private Integer unchangedCount;
    }
}
