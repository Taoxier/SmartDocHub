package com.taoxier.smartdochub.document.controller;

import com.taoxier.smartdochub.common.result.Result;
import com.taoxier.smartdochub.document.service.KgService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/kg")
@RequiredArgsConstructor
@Tag(name = "知识图谱管理")
public class KgController {

    private final KgService kgService;

    @GetMapping("/preview")
    @Operation(summary = "获取知识图谱预览数据")
    public Result<Map<String, Object>> getKnowledgeGraphPreview(
            @Parameter(description = "节点数量限制") @RequestParam(defaultValue = "15") int nodeLimit,
            @Parameter(description = "关系数量限制") @RequestParam(defaultValue = "30") int relationLimit) {
        Map<String, Object> previewData = kgService.getKnowledgeGraphPreview(nodeLimit, relationLimit);
        return Result.success(previewData);
    }
}