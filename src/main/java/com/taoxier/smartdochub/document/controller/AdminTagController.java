package com.taoxier.smartdochub.document.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taoxier.smartdochub.common.result.PageResult;
import com.taoxier.smartdochub.common.result.Result;
import com.taoxier.smartdochub.document.model.entity.Topic;
import com.taoxier.smartdochub.document.service.TopicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "管理-标签管理")
@RestController
@RequestMapping("/api/admin/tag")
@RequiredArgsConstructor
@Slf4j
public class AdminTagController {

    private final TopicService topicService;

    @GetMapping("/list")
    @Operation(summary = "获取所有标签(关键词类型)")
    @PreAuthorize("@ss.hasPerm('doc:tag:query')")
    public Result<List<Topic>> getAllTags() {
        List<Topic> tags = topicService.list(
                new LambdaQueryWrapper<Topic>()
                        .eq(Topic::getTopicType, "KEYWORD")
                        .orderByDesc(Topic::getCreateTime));
        return Result.success(tags);
    }

    @GetMapping("/hot")
    @Operation(summary = "获取热门标签")
    @PreAuthorize("@ss.hasPerm('doc:tag:query')")
    public Result<List<Map<String, Object>>> getHotTags(
            @RequestParam(defaultValue = "30") int limit) {
        List<Topic> topics = topicService.list(
                new LambdaQueryWrapper<Topic>()
                        .eq(Topic::getTopicType, "KEYWORD")
                        .orderByDesc(Topic::getCreateTime)
                        .last("LIMIT " + limit));

        List<Map<String, Object>> result = topics.stream().map(topic -> {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", topic.getId());
            map.put("topicValue", topic.getTopicValue());
            map.put("topicType", topic.getTopicType());
            return map;
        }).collect(Collectors.toList());

        return Result.success(result);
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询标签")
    @PreAuthorize("@ss.hasPerm('doc:tag:query')")
    public PageResult<Topic> queryTagPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        Page<Topic> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Topic> wrapper = new LambdaQueryWrapper<Topic>()
                .eq(Topic::getTopicType, "KEYWORD");

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Topic::getTopicValue, keyword);
        }

        wrapper.orderByDesc(Topic::getCreateTime);
        IPage<Topic> tagPage = topicService.page(page, wrapper);
        return PageResult.success(tagPage);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取标签详情")
    @PreAuthorize("@ss.hasPerm('doc:tag:query')")
    public Result<Topic> getTag(@PathVariable Long id) {
        Topic topic = topicService.getById(id);
        return Result.success(topic);
    }

    @PostMapping
    @Operation(summary = "创建标签")
    @PreAuthorize("@ss.hasPerm('doc:tag:add')")
    public Result<Topic> createTag(@RequestBody Topic topic) {
        topic.setTopicType("KEYWORD");
        topicService.save(topic);
        return Result.success(topic);
    }

    @PutMapping
    @Operation(summary = "更新标签")
    @PreAuthorize("@ss.hasPerm('doc:tag:edit')")
    public Result<Topic> updateTag(@RequestBody Topic topic) {
        topicService.updateById(topic);
        return Result.success(topic);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除标签")
    @PreAuthorize("@ss.hasPerm('doc:tag:delete')")
    public Result<Void> deleteTag(@PathVariable Long id) {
        topicService.removeById(id);
        return Result.success();
    }

    @PostMapping("/batch-delete")
    @Operation(summary = "批量删除标签")
    @PreAuthorize("@ss.hasPerm('doc:tag:delete')")
    public Result<Void> batchDeleteTags(@RequestBody List<Long> ids) {
        topicService.removeByIds(ids);
        return Result.success();
    }
}
