package com.taoxier.smartdochub.translation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.taoxier.smartdochub.translation.model.entity.ConversionTask;
import com.taoxier.smartdochub.translation.service.ConversionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taoxier.smartdochub.translation.mapper.ConversionTaskMapper;
import com.taoxier.smartdochub.document.service.DocumentService;
import com.taoxier.smartdochub.document.model.entity.Document;
import com.taoxier.smartdochub.file.service.FileService;
import com.taoxier.smartdochub.ai.service.AIService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 格式转换服务实现
 */
@Service
@Slf4j
public class ConversionServiceImpl extends ServiceImpl<ConversionTaskMapper, ConversionTask>
        implements ConversionService {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private FileService fileService;

    @Autowired
    private AIService aiService;

    @Override
    public Long submitConversionTask(Long documentId, Long userId, String sourceFormat, String targetFormat) {
        ConversionTask task = new ConversionTask();
        task.setDocumentId(documentId);
        task.setUserId(userId);
        task.setSourceFormat(sourceFormat);
        task.setTargetFormat(targetFormat);
        task.setStatus("PENDING");
        task.setProgress(0);
        task.setCreateTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());

        save(task);

        // 异步处理转换任务
        new Thread(() -> {
            try {
                processConversionTask(task.getId());
            } catch (Exception e) {
                log.error("处理格式转换任务失败: {}", e.getMessage());
                task.setStatus("FAILED");
                task.setErrorMessage(e.getMessage());
                task.setUpdateTime(LocalDateTime.now());
                updateById(task);
            }
        }).start();

        return task.getId();
    }

    @Override
    public ConversionTask getTaskStatus(Long taskId) {
        return getById(taskId);
    }

    @Override
    public void processConversionTask(Long taskId) {
        ConversionTask task = getById(taskId);
        if (task == null) {
            return;
        }

        try {
            // 更新任务状态为处理中
            task.setStatus("PROCESSING");
            task.setProgress(20);
            task.setUpdateTime(LocalDateTime.now());
            updateById(task);

            // 获取文档信息
            Document document = documentService.getById(task.getDocumentId());
            if (document == null) {
                throw new RuntimeException("文档不存在");
            }

            // 使用腾讯云进行格式转换
            String sourceFilePath = document.getStoragePath();
            String convertUrl = fileService.getConvertUrl(sourceFilePath, task.getTargetFormat());

            if (convertUrl == null) {
                throw new RuntimeException("生成转换URL失败");
            }

            // 更新任务状态为完成
            task.setStatus("COMPLETED");
            task.setProgress(100);
            task.setResultPath(convertUrl);
            task.setUpdateTime(LocalDateTime.now());
            updateById(task);

            log.info("格式转换任务完成，任务ID: {}", taskId);
        } catch (Exception e) {
            log.error("格式转换任务失败: {}", e.getMessage());
            task.setStatus("FAILED");
            task.setErrorMessage(e.getMessage());
            task.setUpdateTime(LocalDateTime.now());
            updateById(task);
        }
    }
}