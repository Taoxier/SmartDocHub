package com.taoxier.smartdochub.document.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.taoxier.smartdochub.common.result.PageResult;
import com.taoxier.smartdochub.common.result.Result;
import com.taoxier.smartdochub.core.security.model.SysUserDetails;
import com.taoxier.smartdochub.document.model.dto.DocumentQueryDTO;
import com.taoxier.smartdochub.document.model.dto.DocumentUpdateDTO;
import com.taoxier.smartdochub.document.model.entity.Document;
import com.taoxier.smartdochub.document.model.entity.DocumentVersion;
import com.taoxier.smartdochub.document.model.vo.DocumentDetailVO;
import com.taoxier.smartdochub.document.model.vo.DocumentStatsVO;
import com.taoxier.smartdochub.document.model.vo.DocumentVO;
import com.taoxier.smartdochub.document.service.DocumentService;
import com.taoxier.smartdochub.document.service.UserBehaviorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import com.taoxier.smartdochub.common.exception.BusinessException;
import com.taoxier.smartdochub.common.result.ResultCode;
import com.taoxier.smartdochub.core.security.token.JwtTokenManager;

@Tag(name = "文档管理")
@RestController
@RequestMapping("/api/document")
@RequiredArgsConstructor
@Slf4j
public class DocumentController {

    private final DocumentService documentService;
    private final UserBehaviorService userBehaviorService;
    private final JwtTokenManager jwtTokenManager;

    private Long getCurrentUserId(String token) {
        if (token != null && !token.isEmpty()) {
            // 从令牌中获取用户信息
            try {
                // 验证令牌
                if (!jwtTokenManager.validateToken(token)) {
                    return null;
                }
                // 解析令牌获取用户信息
                Authentication authentication = jwtTokenManager.parseToken(token);
                SysUserDetails userDetails = (SysUserDetails) authentication.getPrincipal();
                return userDetails.getUserId();
            } catch (Exception e) {
                log.error("令牌验证失败", e);
                return null;
            }
        } else {
            // 使用默认的 SecurityContextHolder 方法
            try {
                SysUserDetails userDetails = (SysUserDetails) SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getPrincipal();
                return userDetails.getUserId();
            } catch (Exception e) {
                log.error("获取用户信息失败", e);
                return null;
            }
        }
    }

    private Long getCurrentUserId() {
        return getCurrentUserId(null);
    }

    @PostMapping("/upload")
    @Operation(summary = "上传文档")
    public Result<Document> uploadDocument(
            @Parameter(description = "文档文件") @RequestPart("file") MultipartFile file) {
        Long userId = getCurrentUserId();
        Document document = documentService.uploadAndParse(file, userId);
        return Result.success(document);
    }

    @PostMapping("/upload-multiple")
    @Operation(summary = "批量上传文档")
    public Result<List<Document>> uploadMultipleDocuments(
            @Parameter(description = "文档文件列表") @RequestPart("files") MultipartFile[] files) {
        Long userId = getCurrentUserId();
        List<Document> documents = new ArrayList<>();
        for (MultipartFile file : files) {
            try {
                Document document = documentService.uploadAndParse(file, userId);
                documents.add(document);
            } catch (Exception e) {
                // 继续处理其他文件，不影响整体上传
                log.error("文件上传失败: {}", file.getOriginalFilename(), e);
            }
        }
        return Result.success(documents);
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询文档列表")
    public PageResult<DocumentVO> queryDocumentPage(DocumentQueryDTO queryDTO) {
        Long userId = getCurrentUserId();
        IPage<DocumentVO> page = documentService.queryDocumentPage(queryDTO, userId);
        return PageResult.success(page);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取文档详情")
    public Result<DocumentDetailVO> getDocumentDetail(
            @Parameter(description = "文档ID") @PathVariable Long id) {
        Long userId = getCurrentUserId();
        DocumentDetailVO detail = documentService.getDocumentDetail(id, userId);
        return Result.success(detail);
    }

    @PutMapping
    @Operation(summary = "更新文档元数据")
    public Result<Document> updateDocumentMetadata(@Valid @RequestBody DocumentUpdateDTO updateDTO) {
        Long userId = getCurrentUserId();
        Document document = documentService.updateDocumentMetadata(updateDTO, userId);
        return Result.success(document);
    }

    @PostMapping("/versions/{documentId}")
    @Operation(summary = "上传文档新版本")
    public Result<Document> uploadNewVersion(
            @Parameter(description = "文档ID") @PathVariable Long documentId,
            @Parameter(description = "文档文件") @RequestPart("file") MultipartFile file) {
        Long userId = getCurrentUserId();
        Document document = documentService.uploadNewVersion(file, documentId, userId);
        return Result.success(document);
    }

    @GetMapping("/versions/{documentId}")
    @Operation(summary = "获取文档版本列表")
    public Result<List<DocumentVersion>> getDocumentVersions(
            @Parameter(description = "文档ID") @PathVariable Long documentId) {
        List<DocumentVersion> versions = documentService.getDocumentVersions(documentId);
        return Result.success(versions);
    }

    @GetMapping("/version/{documentId}/{versionNumber}")
    @Operation(summary = "获取指定版本的文档详情")
    public Result<DocumentDetailVO> getDocumentVersionDetail(
            @Parameter(description = "文档ID") @PathVariable Long documentId,
            @Parameter(description = "版本号") @PathVariable Integer versionNumber) {
        Long userId = getCurrentUserId();
        DocumentDetailVO detail = documentService.getDocumentVersionDetail(documentId, versionNumber, userId);
        return Result.success(detail);
    }

    @GetMapping("/version/download/{documentId}/{versionNumber}")
    @Operation(summary = "下载指定版本的文档")
    public void downloadDocumentVersion(
            @Parameter(description = "文档ID") @PathVariable Long documentId,
            @Parameter(description = "版本号") @PathVariable Integer versionNumber,
            HttpServletResponse response) {
        Long userId = getCurrentUserId();
        documentService.downloadDocumentVersion(documentId, versionNumber, userId, response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除文档")
    public Result<Void> deleteDocument(
            @Parameter(description = "文档ID") @PathVariable Long id) {
        Long userId = getCurrentUserId();
        documentService.deleteDocument(id, userId);
        return Result.success();
    }

    @GetMapping("/download/{id}")
    @Operation(summary = "下载文档")
    public void downloadDocument(
            @Parameter(description = "文档ID") @PathVariable Long id,
            HttpServletResponse response) {
        Long userId = getCurrentUserId();
        documentService.downloadDocument(id, userId, response);
    }

    @GetMapping("/check-duplicate")
    @Operation(summary = "检查文档是否重复(通过文件hash)")
    public Result<Document> checkDuplicate(
            @Parameter(description = "文件hash值") @RequestParam String fileHash) {
        Long userId = getCurrentUserId();
        Document existingDoc = documentService.checkDuplicateByHash(fileHash, userId);
        return Result.success(existingDoc);
    }

    @PostMapping("/rate/{id}")
    @Operation(summary = "评分文档")
    public Result<Void> rateDocument(
            @Parameter(description = "文档ID") @PathVariable Long id,
            @Parameter(description = "评分值(1-5)") @RequestParam Byte ratingValue) {
        Long userId = getCurrentUserId();
        userBehaviorService.recordRating(userId, id, ratingValue);
        return Result.success();
    }

    @PostMapping("/search")
    @Operation(summary = "记录搜索行为")
    public Result<Void> recordSearch(
            @Parameter(description = "搜索关键词") @RequestParam String keyword) {
        Long userId = getCurrentUserId();
        userBehaviorService.recordSearch(userId, keyword);
        return Result.success();
    }

    @PostMapping("/favorite/{id}")
    @Operation(summary = "收藏文档")
    public Result<Void> favoriteDocument(
            @Parameter(description = "文档ID") @PathVariable Long id) {
        Long userId = getCurrentUserId();
        userBehaviorService.recordBehavior(userId, id, "FAVORITE");
        // 更新文档的收藏计数
        documentService.updateFavoriteCount(id);
        return Result.success();
    }

    @DeleteMapping("/favorite/{id}")
    @Operation(summary = "取消收藏文档")
    public Result<Void> unfavoriteDocument(
            @Parameter(description = "文档ID") @PathVariable Long id) {
        Long userId = getCurrentUserId();
        // 这里需要实现取消收藏的逻辑
        documentService.removeFavorite(userId, id);
        // 更新文档的收藏计数
        documentService.updateFavoriteCount(id);
        return Result.success();
    }

    @PostMapping("/share/{id}")
    @Operation(summary = "分享文档")
    public Result<String> shareDocument(
            @Parameter(description = "文档ID") @PathVariable Long id) {
        Long userId = getCurrentUserId();
        userBehaviorService.recordBehavior(userId, id, "SHARE");
        // 生成分享链接
        String shareUrl = documentService.generateShareUrl(id);
        return Result.success(shareUrl);
    }

    @GetMapping("/stats")
    @Operation(summary = "获取文档统计数据")
    public Result<DocumentStatsVO> getDocumentStats() {
        DocumentStatsVO stats = documentService.getDocumentStats();
        return Result.success(stats);
    }

    @GetMapping("/my/uploaded")
    @Operation(summary = "获取我的上传文档")
    public PageResult<DocumentVO> getMyUploadedDocuments(DocumentQueryDTO queryDTO) {
        Long userId = getCurrentUserId();
        IPage<DocumentVO> page = documentService.queryMyUploadedDocuments(queryDTO, userId);
        return PageResult.success(page);
    }

    @GetMapping("/my/favorites")
    @Operation(summary = "获取我的收藏文档")
    public PageResult<DocumentVO> getMyFavoriteDocuments(DocumentQueryDTO queryDTO) {
        Long userId = getCurrentUserId();
        IPage<DocumentVO> page = documentService.queryMyFavoriteDocuments(queryDTO, userId);
        return PageResult.success(page);
    }

    @GetMapping("/my/history")
    @Operation(summary = "获取我的浏览历史")
    public PageResult<DocumentVO> getMyHistoryDocuments(DocumentQueryDTO queryDTO) {
        Long userId = getCurrentUserId();
        IPage<DocumentVO> page = documentService.queryMyHistoryDocuments(queryDTO, userId);
        return PageResult.success(page);
    }

    @PostMapping("/favorite/batch-remove")
    @Operation(summary = "批量取消收藏")
    public Result<Void> batchRemoveFavorite(@RequestBody java.util.List<Long> documentIds) {
        Long userId = getCurrentUserId();
        documentService.batchRemoveFavorite(userId, documentIds);
        return Result.success();
    }

    @PostMapping("/history/clear")
    @Operation(summary = "清空浏览历史")
    public Result<Void> clearHistory() {
        Long userId = getCurrentUserId();
        documentService.clearHistory(userId);
        return Result.success();
    }

    @PostMapping("/history/batch-remove")
    @Operation(summary = "批量删除浏览历史")
    public Result<Void> batchRemoveHistory(@RequestBody java.util.List<Long> documentIds) {
        Long userId = getCurrentUserId();
        documentService.batchRemoveHistory(userId, documentIds);
        return Result.success();
    }

    @GetMapping("/kg/{documentId}")
    @Operation(summary = "获取文档知识图谱")
    public Result<java.util.Map<String, Object>> getDocumentKnowledgeGraph(
            @Parameter(description = "文档ID") @PathVariable Long documentId) {
        java.util.Map<String, Object> kgData = documentService.getDocumentKnowledgeGraph(documentId);
        return Result.success(kgData);
    }

    @PostMapping("/kg/{documentId}/rebuild")
    @Operation(summary = "重新构建文档知识图谱")
    public Result<Void> rebuildDocumentKnowledgeGraph(
            @Parameter(description = "文档ID") @PathVariable Long documentId) {
        documentService.rebuildDocumentKnowledgeGraph(documentId);
        return Result.success();
    }

    @GetMapping("/original")
    @Operation(summary = "获取原创力荐文档")
    public Result<List<Document>> getOriginalDocuments(
            @Parameter(description = "返回数量") @RequestParam(defaultValue = "6") int limit) {
        List<Document> documents = documentService.getOriginalDocuments(limit);
        return Result.success(documents);
    }

}
