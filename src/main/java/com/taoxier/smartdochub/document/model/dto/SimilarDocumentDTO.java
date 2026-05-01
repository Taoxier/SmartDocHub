package com.taoxier.smartdochub.document.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SimilarDocumentDTO {
    private Long targetDocumentId;
    private String targetTitle;
    private BigDecimal similarityScore;
    private String similarityType;
    private String category;
    private LocalDateTime createTime;
    private String uploaderName;
    private String storagePath;
}