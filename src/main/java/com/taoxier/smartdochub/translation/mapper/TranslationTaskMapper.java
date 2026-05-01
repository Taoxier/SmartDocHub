package com.taoxier.smartdochub.translation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taoxier.smartdochub.translation.model.entity.TranslationTask;

/**
 * 翻译任务Mapper
 */
public interface TranslationTaskMapper extends BaseMapper<TranslationTask> {

    /**
     * 根据文档ID获取翻译任务列表
     * @param documentId 文档ID
     * @return 翻译任务列表
     */
    java.util.List<TranslationTask> selectByDocumentId(Long documentId);
}