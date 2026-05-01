package com.taoxier.smartdochub.comment.controller;

import com.taoxier.smartdochub.common.result.Result;
import com.taoxier.smartdochub.comment.model.entity.SensitiveWord;
import com.taoxier.smartdochub.comment.service.SensitiveWordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 敏感词管理控制器
 */
@Tag(name = "管理-敏感词管理")
@RestController
@RequestMapping("/api/admin/sensitive-word")
@RequiredArgsConstructor
@Slf4j
public class AdminSensitiveWordController {

    private final SensitiveWordService sensitiveWordService;

    @GetMapping("/list")
    @Operation(summary = "获取敏感词列表")
    public Result<List<SensitiveWord>> getSensitiveWords() {
        try {
            List<SensitiveWord> sensitiveWords = sensitiveWordService.getAllSensitiveWords();
            return Result.success(sensitiveWords);
        } catch (Exception e) {
            log.error("获取敏感词列表失败: {}", e.getMessage());
            return Result.failed("获取敏感词列表失败: " + e.getMessage());
        }
    }

    @PostMapping
    @Operation(summary = "添加敏感词")
    public Result<Void> addSensitiveWord(
            @Parameter(description = "敏感词") @RequestParam String word,
            @Parameter(description = "类别: PORN, VIOLENCE, POLITICS, AD, ILLEGAL") @RequestParam String category,
            @Parameter(description = "级别: 1-警告, 2-拒绝") @RequestParam(defaultValue = "1") int level) {
        try {
            sensitiveWordService.addSensitiveWord(word, category, level);
            return Result.success();
        } catch (Exception e) {
            log.error("添加敏感词失败: {}", e.getMessage());
            return Result.failed("添加敏感词失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除敏感词")
    public Result<Void> deleteSensitiveWord(
            @Parameter(description = "敏感词ID") @PathVariable Long id) {
        try {
            sensitiveWordService.deleteSensitiveWord(id);
            return Result.success();
        } catch (Exception e) {
            log.error("删除敏感词失败: {}", e.getMessage());
            return Result.failed("删除敏感词失败: " + e.getMessage());
        }
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新敏感词缓存")
    public Result<Void> refreshSensitiveWords() {
        try {
            sensitiveWordService.refreshSensitiveWords();
            return Result.success();
        } catch (Exception e) {
            log.error("刷新敏感词缓存失败: {}", e.getMessage());
            return Result.failed("刷新敏感词缓存失败: " + e.getMessage());
        }
    }
}
