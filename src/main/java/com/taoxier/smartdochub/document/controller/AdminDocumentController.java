package com.taoxier.smartdochub.document.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.taoxier.smartdochub.common.result.PageResult;
import com.taoxier.smartdochub.common.result.Result;
import com.taoxier.smartdochub.document.model.dto.DocumentQueryDTO;
import com.taoxier.smartdochub.document.model.entity.Document;
import com.taoxier.smartdochub.document.model.vo.DocumentVO;
import com.taoxier.smartdochub.document.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "管理-文档管理")
@RestController
@RequestMapping("/api/admin/document")
@RequiredArgsConstructor
@Slf4j
public class AdminDocumentController {

    private final DocumentService documentService;

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

    @GetMapping("/upload-trend")
    @Operation(summary = "获取上传趋势")
    @PreAuthorize("@ss.hasPerm('doc:document:query')")
    public Result<List<java.util.Map<String, Object>>> getUploadTrend() {
        List<java.util.Map<String, Object>> trend = documentService.getUploadTrend();
        return Result.success(trend);
    }

    @GetMapping("/type-ratio")
    @Operation(summary = "获取文档类型占比")
    @PreAuthorize("@ss.hasPerm('doc:document:query')")
    public Result<List<java.util.Map<String, Object>>> getDocumentTypeRatio() {
        List<java.util.Map<String, Object>> ratio = documentService.getDocumentTypeRatio();
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
