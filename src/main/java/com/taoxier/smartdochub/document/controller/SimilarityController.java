package com.taoxier.smartdochub.document.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.taoxier.smartdochub.common.result.Result;
import com.taoxier.smartdochub.document.mapper.SimilarityMapper;
import com.taoxier.smartdochub.document.model.dto.SimilarDocumentDTO;
import com.taoxier.smartdochub.document.model.entity.Document;
import com.taoxier.smartdochub.document.model.entity.Similarity;
import com.taoxier.smartdochub.document.service.DocumentService;
import com.taoxier.smartdochub.system.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "文档相似度")
@RestController
@RequestMapping("/api/document")
@RequiredArgsConstructor
@Slf4j
public class SimilarityController {

    private final DocumentService documentService;
    private final SimilarityMapper similarityMapper;
    private final UserService userService;

    @GetMapping("/similar/{docId}")
    @Operation(summary = "获取相似文档列表")
    public Result<List<SimilarDocumentDTO>> getSimilarDocuments(
            @Parameter(description = "文档ID") @PathVariable Long docId,
            @Parameter(description = "相似度阈值（0-100）") @RequestParam(defaultValue = "0") double threshold,
            @Parameter(description = "返回数量限制") @RequestParam(defaultValue = "10") int limit) {

        try {
            Document targetDoc = documentService.getById(docId);
            if (targetDoc == null) {
                return Result.failed("文档不存在");
            }

            LambdaQueryWrapper<Similarity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Similarity::getSourceDocId, docId)
                    .eq(Similarity::getSimilarityType, "TEXT")
                    .orderByDesc(Similarity::getSimilarityScore)
                    .last("LIMIT " + limit);

            List<Similarity> similarities = similarityMapper.selectList(wrapper);

            if (threshold > 0) {
                similarities = similarities.stream()
                        .filter(s -> s.getSimilarityScore().doubleValue() >= threshold / 100.0)
                        .collect(Collectors.toList());
            }

            List<Long> targetDocIds = similarities.stream()
                    .map(Similarity::getTargetDocId)
                    .collect(Collectors.toList());

            if (targetDocIds.isEmpty()) {
                return Result.success(new ArrayList<>());
            }

            List<Document> targetDocs = documentService.listByIds(targetDocIds);
            Map<Long, Document> docMap = targetDocs.stream()
                    .collect(Collectors.toMap(Document::getId, d -> d));

            Map<Long, String> userNameMap = userService.getUserNameMap(
                    targetDocs.stream()
                            .map(Document::getUploadUserId)
                            .collect(Collectors.toSet())
            );

            List<SimilarDocumentDTO> result = new ArrayList<>();
            for (Similarity sim : similarities) {
                Document doc = docMap.get(sim.getTargetDocId());
                if (doc == null) continue;

                SimilarDocumentDTO dto = new SimilarDocumentDTO();
                dto.setTargetDocumentId(doc.getId());
                dto.setTargetTitle(doc.getTitle());
                dto.setSimilarityScore(sim.getSimilarityScore());
                dto.setSimilarityType(sim.getSimilarityType());
                dto.setCategory(doc.getCategory());
                dto.setCreateTime(doc.getCreateTime());
                dto.setUploaderName(userNameMap.getOrDefault(doc.getUploadUserId(), "未知"));
                dto.setStoragePath(doc.getStoragePath());
                result.add(dto);
            }

            return Result.success(result);
        } catch (Exception e) {
            log.error("获取相似文档失败: {}", e.getMessage(), e);
            return Result.failed("获取相似文档失败: " + e.getMessage());
        }
    }
}