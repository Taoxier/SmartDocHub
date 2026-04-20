package com.taoxier.smartdochub.document.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author taoxier
 * @Date 2025/10/20 下午5:33
 * @描述
 */
@Data
public class SimilarityResultDTO {
    private Long documentId;
    private BigDecimal overallSimilarity;
    private BigDecimal textSimilarity;
    private BigDecimal tableSimilarity;
    private BigDecimal formulaSimilarity;
    private List<SimilarDocumentDTO> similarDocuments;
    private LocalDateTime detectionTime;
}

