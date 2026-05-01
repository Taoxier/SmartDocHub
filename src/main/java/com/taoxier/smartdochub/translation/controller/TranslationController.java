package com.taoxier.smartdochub.translation.controller;

import com.taoxier.smartdochub.common.result.Result;
import com.taoxier.smartdochub.translation.model.entity.TranslationTask;
import com.taoxier.smartdochub.translation.service.TranslationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 翻译控制器
 */
@Tag(name = "文档翻译")
@RestController
@RequestMapping("/api/document")
@RequiredArgsConstructor
@Slf4j
public class TranslationController {

    private final TranslationService translationService;

    @PostMapping("/translate")
    @Operation(summary = "提交翻译任务")
    public Result<Long> submitTranslationTask(
            @Parameter(description = "文档ID") @RequestParam Long documentId,
            @Parameter(description = "用户ID") @RequestParam Long userId,
            @Parameter(description = "源语言") @RequestParam String sourceLanguage,
            @Parameter(description = "目标语言") @RequestParam String targetLanguage) {
        try {
            Long taskId = translationService.submitTranslationTask(documentId, userId, sourceLanguage, targetLanguage);
            return Result.success(taskId);
        } catch (Exception e) {
            log.error("提交翻译任务失败: {}", e.getMessage());
            return Result.failed("提交翻译任务失败: " + e.getMessage());
        }
    }

    @GetMapping("/task/{taskId}")
    @Operation(summary = "查询任务状态")
    public Result<TranslationTask> getTaskStatus(
            @Parameter(description = "任务ID") @PathVariable Long taskId) {
        try {
            TranslationTask task = translationService.getTaskStatus(taskId);
            if (task == null) {
                return Result.failed("任务不存在");
            }
            return Result.success(task);
        } catch (Exception e) {
            log.error("查询任务状态失败: {}", e.getMessage());
            return Result.failed("查询任务状态失败: " + e.getMessage());
        }
    }
}