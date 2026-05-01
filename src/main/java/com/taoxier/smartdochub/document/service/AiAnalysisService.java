package com.taoxier.smartdochub.document.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.taoxier.smartdochub.document.model.entity.AiAnalysisResult;

/**
 * AI分析服务
 */
public interface AiAnalysisService extends IService<AiAnalysisResult> {

    /**
     * 保存或更新AI分析结果
     * @param documentId 文档ID
     * @param aiProbability AI生成概率
     * @param confidence 置信度
     * @param detectedModel 检测到的模型
     * @param keyFeatures 关键特征
     * @param result 分析结果
     */
    void saveOrUpdateAnalysisResult(Long documentId, double aiProbability, double confidence, 
                                  String detectedModel, String keyFeatures, String result);

    /**
     * 根据文档ID获取AI分析结果
     * @param documentId 文档ID
     * @return AI分析结果
     */
    AiAnalysisResult getByDocumentId(Long documentId);
}