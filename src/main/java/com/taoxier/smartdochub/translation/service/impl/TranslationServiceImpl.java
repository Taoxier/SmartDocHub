package com.taoxier.smartdochub.translation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.taoxier.smartdochub.translation.model.entity.TranslationTask;
import com.taoxier.smartdochub.translation.service.TranslationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taoxier.smartdochub.translation.mapper.TranslationTaskMapper;
import com.taoxier.smartdochub.document.service.DocumentService;
import com.taoxier.smartdochub.file.service.FileService;
import com.taoxier.smartdochub.ai.service.AIService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 翻译服务实现
 */
@Service
@Slf4j
public class TranslationServiceImpl extends ServiceImpl<TranslationTaskMapper, TranslationTask>
        implements TranslationService {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private FileService fileService;

    @Autowired
    private AIService aiService;

    @Override
    public Long submitTranslationTask(Long documentId, Long userId, String sourceLanguage, String targetLanguage) {
        TranslationTask task = new TranslationTask();
        task.setDocumentId(documentId);
        task.setUserId(userId);
        task.setSourceLanguage(sourceLanguage);
        task.setTargetLanguage(targetLanguage);
        task.setStatus("PENDING");
        task.setProgress(0);
        task.setCreateTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());

        save(task);

        // 异步处理翻译任务
        new Thread(() -> {
            try {
                processTranslationTask(task.getId());
            } catch (Exception e) {
                log.error("处理翻译任务失败: {}", e.getMessage());
                task.setStatus("FAILED");
                task.setErrorMessage(e.getMessage());
                task.setUpdateTime(LocalDateTime.now());
                updateById(task);
            }
        }).start();

        return task.getId();
    }

    @Override
    public TranslationTask getTaskStatus(Long taskId) {
        return getById(taskId);
    }

    @Override
    public void processTranslationTask(Long taskId) {
        TranslationTask task = getById(taskId);
        if (task == null) {
            return;
        }

        try {
            // 更新任务状态为处理中
            task.setStatus("PROCESSING");
            task.setProgress(20);
            task.setUpdateTime(LocalDateTime.now());
            updateById(task);

            // 调用Coze API进行翻译
            String resultPath = aiService.translateDocument(task.getDocumentId(),
                    task.getSourceLanguage(), task.getTargetLanguage());

            // 更新任务状态为完成
            task.setStatus("COMPLETED");
            task.setProgress(100);
            task.setResultPath(resultPath);
            task.setUpdateTime(LocalDateTime.now());
            updateById(task);

            log.info("翻译任务完成，任务ID: {}", taskId);
        } catch (Exception e) {
            log.error("翻译任务失败: {}", e.getMessage());
            task.setStatus("FAILED");
            task.setErrorMessage(e.getMessage());
            task.setUpdateTime(LocalDateTime.now());
            updateById(task);
        }
    }
}