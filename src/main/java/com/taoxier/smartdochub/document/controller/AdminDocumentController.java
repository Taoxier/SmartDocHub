package com.taoxier.smartdochub.document.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.taoxier.smartdochub.common.result.PageResult;
import com.taoxier.smartdochub.common.result.Result;
import com.taoxier.smartdochub.document.model.dto.DocumentQueryDTO;
import com.taoxier.smartdochub.document.model.entity.Document;
import com.taoxier.smartdochub.document.model.vo.DocumentVO;
import com.taoxier.smartdochub.document.service.DocumentService;
import com.taoxier.smartdochub.file.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "管理-文档管理")
@RestController
@RequestMapping("/api/admin/document")
@RequiredArgsConstructor
@Slf4j
public class AdminDocumentController {

    private final DocumentService documentService;
    private final FileService fileService;

    @GetMapping("/page")
    @Operation(summary = "分页查询文档列表(管理员)")
    @PreAuthorize("@ss.hasPerm('doc:document:query')")
    public PageResult<DocumentVO> queryDocumentPage(DocumentQueryDTO queryDTO) {
        IPage<DocumentVO> page = documentService.queryAdminDocumentPage(queryDTO);
        return PageResult.success(page);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取文档详情(管理员)")
    @PreAuthorize("@ss.hasPerm('doc:document:query')")
    public Result<Document> getDocument(@PathVariable Long id) {
        Document document = documentService.getById(id);
        return Result.success(document);
    }

    @GetMapping("/{id}/preview-url")
    @Operation(summary = "获取文档预览URL(管理员)")
    @PreAuthorize("@ss.hasPerm('doc:document:query')")
    public Result<Map<String, String>> getPreviewUrl(@PathVariable Long id) {
        Document document = documentService.getById(id);
        if (document == null) {
            return Result.failed("文档不存在");
        }
        String previewUrl = fileService.getPreviewUrl(document.getStoragePath());
        if (previewUrl == null) {
            return Result.failed("该文件类型不支持预览");
        }
        return Result.success(Map.of("previewUrl", previewUrl));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除文档(管理员)")
    @PreAuthorize("@ss.hasPerm('doc:document:delete')")
    public Result<Void> deleteDocument(@PathVariable Long id) {
        documentService.adminDeleteDocument(id);
        return Result.success();
    }

    @PostMapping("/batch-delete")
    @Operation(summary = "批量删除文档(管理员)")
    @PreAuthorize("@ss.hasPerm('doc:document:delete')")
    public Result<Void> batchDeleteDocuments(@RequestBody List<Long> ids) {
        documentService.batchDeleteDocuments(ids);
        return Result.success();
    }

    @PutMapping("/{id}/approve")
    @Operation(summary = "审核通过文档(管理员)")
    @PreAuthorize("@ss.hasPerm('doc:document:audit')")
    public Result<Void> approveDocument(@PathVariable Long id) {
        documentService.updateAuditStatus(id, "APPROVED", "管理员审核通过");
        return Result.success();
    }

    @PutMapping("/{id}/reject")
    @Operation(summary = "审核拒绝文档(管理员)")
    @PreAuthorize("@ss.hasPerm('doc:document:audit')")
    public Result<Void> rejectDocument(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.get("reason") : null;
        documentService.updateAuditStatus(id, "REJECTED", reason != null ? reason : "管理员审核拒绝");
        return Result.success();
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新文档信息(管理员)")
    @PreAuthorize("@ss.hasPerm('doc:document:edit')")
    public Result<Void> updateDocument(@PathVariable Long id, @RequestBody Document document) {
        document.setId(id);
        documentService.updateById(document);
        return Result.success();
    }

    @GetMapping("/upload-trend")
    @Operation(summary = "获取上传趋势")
    @PreAuthorize("@ss.hasPerm('doc:document:query')")
    public Result<List<Map<String, Object>>> getUploadTrend() {
        List<Map<String, Object>> trend = documentService.getUploadTrend();
        return Result.success(trend);
    }

    @GetMapping("/type-ratio")
    @Operation(summary = "获取文档类型占比")
    @PreAuthorize("@ss.hasPerm('doc:document:query')")
    public Result<List<Map<String, Object>>> getDocumentTypeRatio() {
        List<Map<String, Object>> ratio = documentService.getDocumentTypeRatio();
        return Result.success(ratio);
    }

    @GetMapping("/stats")
    @Operation(summary = "获取文档统计数据")
    @PreAuthorize("@ss.hasPerm('doc:document:query')")
    public Result<Object> getAdminDocumentStats() {
        Object stats = documentService.getAdminDocumentStats();
        return Result.success(stats);
    }
}
