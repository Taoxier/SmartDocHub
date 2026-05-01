package com.taoxier.smartdochub.translation.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.taoxier.smartdochub.translation.model.entity.ConversionTask;

/**
 * 格式转换服务
 */
public interface ConversionService extends IService<ConversionTask> {

    /**
     * 提交格式转换任务
     * @param documentId 文档ID
     * @param userId 用户ID
     * @param sourceFormat 源格式
     * @param targetFormat 目标格式
     * @return 任务ID
     */
    Long submitConversionTask(Long documentId, Long userId, String sourceFormat, String targetFormat);

    /**
     * 查询任务状态
     * @param taskId 任务ID
     * @return 任务信息
     */
    ConversionTask getTaskStatus(Long taskId);

    /**
     * 处理格式转换任务
     * @param taskId 任务ID
     */
    void processConversionTask(Long taskId);
}