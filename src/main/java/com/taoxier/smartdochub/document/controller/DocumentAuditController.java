package com.taoxier.smartdochub.document.controller;

import com.taoxier.smartdochub.common.result.Result;
import com.taoxier.smartdochub.document.model.entity.Document;
import com.taoxier.smartdochub.document.model.entity.DocumentAudit;
import com.taoxier.smartdochub.document.service.DocumentAuditService;
import com.taoxier.smartdochub.document.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "文档审核")
@RestController
@RequestMapping("/api/document")
@RequiredArgsConstructor
@Slf4j
public class DocumentAuditController {

    private final DocumentAuditService documentAuditService;
    private final DocumentService documentService;

    @PostMapping("/audit/content")
    @Operation(summary = "提交文档审核")
    public Result<Long> submitAudit(
            @Parameter(description = "文档ID") @RequestParam Long documentId,
            @Parameter(description = "审核类型") @RequestParam String auditType) {
        try {
            Document document = documentService.getById(documentId);
            if (document == null) {
                return Result.failed("文档不存在");
            }
            if (document.getStoragePath() == null || document.getStoragePath().isEmpty()) {
                return Result.failed("文档存储路径不存在");
            }
            Long auditId = documentAuditService.submitAudit(documentId, auditType, document.getStoragePath());
            return Result.success(auditId);
        } catch (Exception e) {
            log.error("提交审核失败: {}", e.getMessage());
            return Result.failed("提交审核失败: " + e.getMessage());
        }
    }

    @GetMapping("/audit/status/{docId}")
    @Operation(summary = "查询审核状态")
    public Result<DocumentAudit> getAuditStatus(
            @Parameter(description = "文档ID") @PathVariable Long docId) {
        try {
            DocumentAudit audit = documentAuditService.getAuditStatus(docId);
            if (audit == null) {
                return Result.failed("审核记录不存在");
            }
            return Result.success(audit);
        } catch (Exception e) {
            log.error("查询审核状态失败: {}", e.getMessage());
            return Result.failed("获取审核状态失败: " + e.getMessage());
        }
    }

    @GetMapping("/audit/list/{docId}")
    @Operation(summary = "查询文档审核记录列表")
    public Result<List<DocumentAudit>> getAuditList(
            @Parameter(description = "文档ID") @PathVariable Long docId) {
        try {
            List<DocumentAudit> list = documentAuditService.listByDocumentId(docId);
            return Result.success(list);
        } catch (Exception e) {
            log.error("查询审核记录列表失败: {}", e.getMessage());
            return Result.failed("获取审核记录列表失败: " + e.getMessage());
        }
    }
}