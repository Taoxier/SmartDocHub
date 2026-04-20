package com.taoxier.smartdochub.document.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SimilarDocumentDTO {
    private Long targetDocumentId;
    private String targetTitle;
    private BigDecimal similarityScore;
    private String similarityType;
    private List<String> matchedSections;
}
