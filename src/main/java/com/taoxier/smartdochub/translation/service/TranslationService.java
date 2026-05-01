package com.taoxier.smartdochub.translation.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.taoxier.smartdochub.translation.model.entity.TranslationTask;

/**
 * 翻译服务
 */
public interface TranslationService extends IService<TranslationTask> {

    /**
     * 提交翻译任务
     * @param documentId 文档ID
     * @param userId 用户ID
     * @param sourceLanguage 源语言
     * @param targetLanguage 目标语言
     * @return 任务ID
     */
    Long submitTranslationTask(Long documentId, Long userId, String sourceLanguage, String targetLanguage);

    /**
     * 查询任务状态
     * @param taskId 任务ID
     * @return 任务信息
     */
    TranslationTask getTaskStatus(Long taskId);

    /**
     * 处理翻译任务
     * @param taskId 任务ID
     */
    void processTranslationTask(Long taskId);
}