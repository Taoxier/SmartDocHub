package com.taoxier.smartdochub.comment.service;

import com.taoxier.smartdochub.comment.model.entity.SensitiveWord;

import java.util.List;
import java.util.Map;

/**
 * 敏感词服务接口
 */
public interface SensitiveWordService {

    /**
     * 检测文本中的敏感词
     * @param text 待检测文本
     * @return 检测结果，包含是否包含敏感词、敏感词类别、具体敏感词
     */
    Map<String, Object> detectSensitiveWords(String text);

    /**
     * 根据检测结果获取审核状态
     * @param detectionResult 检测结果
     * @return 审核状态：APPROVED, MANUAL_REVIEW, REJECTED
     */
    String getAuditStatus(Map<String, Object> detectionResult);

    /**
     * 刷新敏感词缓存
     */
    void refreshSensitiveWords();

    /**
     * 获取所有敏感词
     * @return 敏感词列表
     */
    List<SensitiveWord> getAllSensitiveWords();

    /**
     * 添加敏感词
     * @param word 敏感词
     * @param category 类别
     * @param level 级别
     */
    void addSensitiveWord(String word, String category, int level);

    /**
     * 删除敏感词
     * @param id 敏感词ID
     */
    void deleteSensitiveWord(Long id);
}
