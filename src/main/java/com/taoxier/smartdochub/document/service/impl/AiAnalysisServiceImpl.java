package com.taoxier.smartdochub.document.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.taoxier.smartdochub.document.model.entity.AiAnalysisResult;
import com.taoxier.smartdochub.document.service.AiAnalysisService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taoxier.smartdochub.document.mapper.AiAnalysisResultMapper;
import org.springframework.stereotype.Service;

/**
 * AI分析服务实现
 */
@Service
public class AiAnalysisServiceImpl extends ServiceImpl<AiAnalysisResultMapper, AiAnalysisResult> implements AiAnalysisService {

    @Override
    public void saveOrUpdateAnalysisResult(Long documentId, double aiProbability, double confidence, 
                                         String detectedModel, String keyFeatures, String result) {
        AiAnalysisResult existing = getByDocumentId(documentId);
        if (existing != null) {
            existing.setAiProbability(java.math.BigDecimal.valueOf(aiProbability));
            existing.setConfidence(java.math.BigDecimal.valueOf(confidence));
            existing.setDetectedModel(detectedModel);
            existing.setKeyFeatures(keyFeatures);
            existing.setResult(result);
            updateById(existing);
        } else {
            AiAnalysisResult newResult = new AiAnalysisResult();
            newResult.setDocumentId(documentId);
            newResult.setAiProbability(java.math.BigDecimal.valueOf(aiProbability));
            newResult.setConfidence(java.math.BigDecimal.valueOf(confidence));
            newResult.setDetectedModel(detectedModel);
            newResult.setKeyFeatures(keyFeatures);
            newResult.setResult(result);
            save(newResult);
        }
    }

    @Override
    public AiAnalysisResult getByDocumentId(Long documentId) {
        return getOne(new LambdaQueryWrapper<AiAnalysisResult>()
                .eq(AiAnalysisResult::getDocumentId, documentId));
    }
}