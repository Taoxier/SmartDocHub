package com.taoxier.smartdochub.document.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.taoxier.smartdochub.common.result.Result;
import com.taoxier.smartdochub.document.model.entity.ContentChunk;
import com.taoxier.smartdochub.document.service.ContentChunkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 文档分块控制器
 */
@Tag(name = "文档分块")
@RestController
@RequestMapping("/api/document")
@RequiredArgsConstructor
@Slf4j
public class ContentChunkController {

    private final ContentChunkService contentChunkService;

    @GetMapping("/chunks/{docId}")
    @Operation(summary = "获取文档分块列表")
    public Result<List<ContentChunk>> getDocumentChunks(
            @Parameter(description = "文档ID") @PathVariable Long docId) {
        try {
            List<ContentChunk> chunks = contentChunkService.list(new LambdaQueryWrapper<ContentChunk>()
                    .eq(ContentChunk::getDocumentId, docId)
                    .orderByAsc(ContentChunk::getVersionNumber)
                    .orderByAsc(ContentChunk::getChunkIndex));
            return Result.success(chunks);
        } catch (Exception e) {
            log.error("获取文档分块失败: {}", e.getMessage());
            return Result.failed("获取文档分块失败: " + e.getMessage());
        }
    }
}