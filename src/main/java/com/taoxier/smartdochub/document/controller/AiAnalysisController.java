package com.taoxier.smartdochub.document.controller;

import com.taoxier.smartdochub.common.result.Result;
import com.taoxier.smartdochub.document.model.entity.AiAnalysisResult;
import com.taoxier.smartdochub.document.service.AiAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI分析控制器
 */
@Tag(name = "AI分析")
@RestController
@RequestMapping("/api/document")
@RequiredArgsConstructor
@Slf4j
public class AiAnalysisController {

    private final AiAnalysisService aiAnalysisService;

    @GetMapping("/ai-rate/{docId}")
    @Operation(summary = "获取AI率分析结果")
    public Result<AiAnalysisResult> getAiAnalysisResult(
            @Parameter(description = "文档ID") @PathVariable Long docId) {
        try {
            AiAnalysisResult result = aiAnalysisService.getByDocumentId(docId);
            if (result == null) {
                return Result.failed("AI分析结果不存在");
            }
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取AI分析结果失败: {}", e.getMessage());
            return Result.failed("获取AI率分析结果失败: " + e.getMessage());
        }
    }
}