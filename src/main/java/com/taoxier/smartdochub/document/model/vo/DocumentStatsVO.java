package com.taoxier.smartdochub.document.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "文档统计数据VO")
public class DocumentStatsVO {

    @Schema(description = "文档总数")
    private Integer totalDocs;

    @Schema(description = "总浏览量")
    private Integer totalViews;

    @Schema(description = "总下载量")
    private Integer totalDownloads;

    @Schema(description = "总收藏量")
    private Integer totalFavorites;

    @Schema(description = "平均评分")
    private BigDecimal avgRating;

}