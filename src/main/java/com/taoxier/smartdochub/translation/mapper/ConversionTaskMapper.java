package com.taoxier.smartdochub.translation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taoxier.smartdochub.translation.model.entity.ConversionTask;

/**
 * 格式转换任务Mapper
 */
public interface ConversionTaskMapper extends BaseMapper<ConversionTask> {

    /**
     * 根据文档ID获取转换任务列表
     * @param documentId 文档ID
     * @return 转换任务列表
     */
    java.util.List<ConversionTask> selectByDocumentId(Long documentId);
}