package com.taoxier.smartdochub.document.controller;

import com.taoxier.smartdochub.common.result.Result;
import com.taoxier.smartdochub.document.service.TopicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/topic")
@RequiredArgsConstructor
@Tag(name = "标签管理")
public class TopicController {

    private final TopicService topicService;

    @GetMapping("/hot")
    @Operation(summary = "获取热门标签")
    public Result<List<Map<String, Object>>> getHotTopics(
            @Parameter(description = "返回数量") @RequestParam(defaultValue = "30") int limit) {
        List<Map<String, Object>> topics = topicService.getHotTopics(limit);
        return Result.success(topics);
    }
}