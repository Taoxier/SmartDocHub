package com.taoxier.smartdochub.document.service;

import com.taoxier.smartdochub.document.model.dto.SimilarDocumentDTO;
import com.taoxier.smartdochub.document.model.dto.SimilarityResultDTO;

import java.util.List;

/**
 * @Author taoxier
 * @Date 2025/10/20 下午5:34
 * @描述 检测重复率
 */
public interface SimilarityDetectionService {
    /**
     * 检测文档与现有文库的重复率
     */
    SimilarityResultDTO checkSimilarity(Long documentId, String content);

    /**
     * 批量检测文档相似度
     */
    List<SimilarityResultDTO> batchCheckSimilarity(List<Long> documentIds);

    /**
     * 获取文档的相似文档列表
     */
    List<SimilarDocumentDTO> getSimilarDocuments(Long documentId);
}
