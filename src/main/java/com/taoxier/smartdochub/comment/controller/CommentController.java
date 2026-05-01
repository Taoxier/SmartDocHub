package com.taoxier.smartdochub.comment.controller;

import com.taoxier.smartdochub.ai.service.AIService;
import com.taoxier.smartdochub.common.result.Result;
import com.taoxier.smartdochub.comment.model.entity.Comment;
import com.taoxier.smartdochub.comment.model.vo.CommentAuditResultVO;
import com.taoxier.smartdochub.comment.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 评论控制器
 */
@Tag(name = "评论管理")
@RestController
@RequestMapping("/api/comment")
@RequiredArgsConstructor
@Slf4j
public class CommentController {

    private final CommentService commentService;
    private final AIService aiService;

    @PostMapping
    @Operation(summary = "添加评论")
    public Result<Long> addComment(@RequestBody Comment comment) {
        try {
            Long commentId = commentService.addComment(comment);
            return Result.success(commentId);
        } catch (Exception e) {
            log.error("添加评论失败: {}", e.getMessage());
            return Result.failed("添加评论失败: " + e.getMessage());
        }
    }

    @GetMapping("/document/{docId}")
    @Operation(summary = "获取文档评论列表")
    public Result<List<Comment>> getCommentsByDocumentId(
            @Parameter(description = "文档ID") @PathVariable Long docId) {
        try {
            List<Comment> comments = commentService.getCommentsByDocumentId(docId);
            return Result.success(comments);
        } catch (Exception e) {
            log.error("获取评论列表失败: {}", e.getMessage());
            return Result.failed("获取评论失败: " + e.getMessage());
        }
    }

    @PostMapping("/audit/ai")
    @Operation(summary = "触发AI审核")
    public Result<String> auditComment(@RequestBody Comment comment) {
        try {
            commentService.addComment(comment);
            return Result.success("审核任务已触发");
        } catch (Exception e) {
            log.error("触发审核失败: {}", e.getMessage());
            return Result.failed("AI审核失败: " + e.getMessage());
        }
    }

    @GetMapping("/audit/result/{commentId}")
    @Operation(summary = "获取审核结果")
    public Result<Comment> getAuditResult(
            @Parameter(description = "评论ID") @PathVariable Long commentId) {
        try {
            Comment comment = commentService.getById(commentId);
            if (comment == null) {
                return Result.failed("评论不存在");
            }
            return Result.success(comment);
        } catch (Exception e) {
            log.error("获取审核结果失败: {}", e.getMessage());
            return Result.failed("获取审核结果失败: " + e.getMessage());
        }
    }

    @PostMapping("/audit/detail")
    @Operation(summary = "获取评论详细审核结果（敏感词+AI情感双审核）")
    public Result<CommentAuditResultVO> getCommentAuditDetail(@RequestBody Comment comment) {
        try {
            CommentAuditResultVO auditResult = aiService.auditCommentDetail(comment.getContent());
            return Result.success(auditResult);
        } catch (Exception e) {
            log.error("获取详细审核结果失败: {}", e.getMessage());
            return Result.failed("获取详细审核结果失败: " + e.getMessage());
        }
    }
}