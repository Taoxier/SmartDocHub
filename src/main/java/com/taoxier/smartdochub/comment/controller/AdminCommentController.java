package com.taoxier.smartdochub.comment.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taoxier.smartdochub.comment.model.entity.Comment;
import com.taoxier.smartdochub.comment.service.CommentService;
import com.taoxier.smartdochub.common.result.PageResult;
import com.taoxier.smartdochub.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "管理-评论管理")
@RestController
@RequestMapping("/api/admin/comment")
@RequiredArgsConstructor
@Slf4j
public class AdminCommentController {

    private final CommentService commentService;

    @GetMapping("/page")
    @Operation(summary = "分页查询评论列表(管理员)")
    @PreAuthorize("@ss.hasPerm('doc:comment:query')")
    public PageResult<Comment> queryCommentPage(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") int pageSize,
            @Parameter(description = "文档ID（可选）") @RequestParam(required = false) Long documentId,
            @Parameter(description = "审核状态（可选：APPROVED, REJECTED, MANUAL_REVIEW, PENDING）") @RequestParam(required = false) String auditStatus,
            @Parameter(description = "关键词搜索（可选）") @RequestParam(required = false) String keyword) {

        Page<Comment> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();

        if (documentId != null) {
            wrapper.eq(Comment::getDocumentId, documentId);
        }

        if (auditStatus != null && !auditStatus.isEmpty()) {
            wrapper.eq(Comment::getAuditStatus, auditStatus);
        }

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Comment::getContent, keyword);
        }

        wrapper.eq(Comment::getStatus, 0);
        wrapper.orderByDesc(Comment::getCreateTime);

        IPage<Comment> resultPage = commentService.page(page, wrapper);
        return PageResult.success(resultPage);
    }

    @PutMapping("/{id}/audit")
    @Operation(summary = "人工审核评论")
    @PreAuthorize("@ss.hasPerm('doc:comment:audit')")
    public Result<Void> auditComment(
            @Parameter(description = "评论ID") @PathVariable Long id,
            @Parameter(description = "审核结果（APPROVED-通过，REJECTED-拒绝）") @RequestParam String result,
            @Parameter(description = "审核原因（可选）") @RequestParam(required = false) String reason) {

        Comment comment = commentService.getById(id);
        if (comment == null) {
            return Result.failed("评论不存在");
        }

        comment.setAuditStatus(result);
        if (reason != null && !reason.isEmpty()) {
            comment.setAuditReason(reason);
        }
        comment.setAuditTime(LocalDateTime.now());
        commentService.updateById(comment);

        return Result.success();
    }

    @PostMapping("/batch-audit")
    @Operation(summary = "批量审核评论")
    @PreAuthorize("@ss.hasPerm('doc:comment:audit')")
    public Result<Void> batchAuditComments(
            @Parameter(description = "评论ID列表") @RequestBody BatchAuditRequest request) {

        if (request.getIds() == null || request.getIds().isEmpty()) {
            return Result.failed("请选择要审核的评论");
        }

        for (Long id : request.getIds()) {
            Comment comment = commentService.getById(id);
            if (comment != null) {
                comment.setAuditStatus(request.getResult());
                comment.setAuditTime(LocalDateTime.now());
                if (request.getReason() != null) {
                    comment.setAuditReason(request.getReason());
                }
                commentService.updateById(comment);
            }
        }

        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除评论")
    @PreAuthorize("@ss.hasPerm('doc:comment:delete')")
    public Result<Void> deleteComment(@PathVariable Long id) {
        Comment comment = commentService.getById(id);
        if (comment == null) {
            return Result.failed("评论不存在");
        }
        comment.setStatus((byte) 1);
        comment.setUpdateTime(LocalDateTime.now());
        commentService.updateById(comment);
        return Result.success();
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取评论详情")
    @PreAuthorize("@ss.hasPerm('doc:comment:query')")
    public Result<Comment> getComment(@PathVariable Long id) {
        Comment comment = commentService.getById(id);
        if (comment == null) {
            return Result.failed("评论不存在");
        }
        return Result.success(comment);
    }

    public static class BatchAuditRequest {
        private List<Long> ids;
        private String result;
        private String reason;

        public List<Long> getIds() {
            return ids;
        }

        public void setIds(List<Long> ids) {
            this.ids = ids;
        }

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }
}