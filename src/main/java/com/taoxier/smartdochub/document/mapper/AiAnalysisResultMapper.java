package com.taoxier.smartdochub.document.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taoxier.smartdochub.document.model.entity.AiAnalysisResult;

/**
 * AI分析结果Mapper
 */
public interface AiAnalysisResultMapper extends BaseMapper<AiAnalysisResult> {

    /**
     * 根据文档ID获取分析结果
     * @param documentId 文档ID
     * @return AI分析结果
     */
    AiAnalysisResult selectByDocumentId(Long documentId);
}