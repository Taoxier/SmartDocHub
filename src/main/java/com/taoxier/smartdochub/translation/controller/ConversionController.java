package com.taoxier.smartdochub.translation.controller;

import com.taoxier.smartdochub.common.result.Result;
import com.taoxier.smartdochub.translation.model.entity.ConversionTask;
import com.taoxier.smartdochub.translation.service.ConversionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 格式转换控制器
 */
@Tag(name = "文档格式转换")
@RestController
@RequestMapping("/api/document")
@RequiredArgsConstructor
@Slf4j
public class ConversionController {

    private final ConversionService conversionService;

    @PostMapping("/convert")
    @Operation(summary = "提交格式转换任务")
    public Result<Long> submitConversionTask(
            @Parameter(description = "文档ID") @RequestParam Long documentId,
            @Parameter(description = "用户ID") @RequestParam Long userId,
            @Parameter(description = "源格式") @RequestParam String sourceFormat,
            @Parameter(description = "目标格式") @RequestParam String targetFormat) {
        try {
            Long taskId = conversionService.submitConversionTask(documentId, userId, sourceFormat, targetFormat);
            return Result.success(taskId);
        } catch (Exception e) {
            log.error("提交格式转换任务失败: {}", e.getMessage());
            return Result.failed("提交格式转换任务失败: " + e.getMessage());
        }
    }

    @GetMapping("/task/convert/{taskId}")
    @Operation(summary = "查询格式转换任务状态")
    public Result<ConversionTask> getTaskStatus(
            @Parameter(description = "任务ID") @PathVariable Long taskId) {
        try {
            ConversionTask task = conversionService.getTaskStatus(taskId);
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