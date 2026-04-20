package com.taoxier.smartdochub.document.controller;

import com.taoxier.smartdochub.common.result.Result;
import com.taoxier.smartdochub.document.model.entity.Document;
import com.taoxier.smartdochub.document.service.DocumentService;
import com.taoxier.smartdochub.document.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommend")
public class RecommendationController {

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private DocumentService documentService;

    @GetMapping("/user")
    public Result getUserRecommendations(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "false") boolean refresh) {
        List<Long> recommendations = recommendationService.getRecommendations(userId, limit, offset, refresh);
        return Result.success(recommendations);
    }

    @PostMapping("/documents")

    public Result getRecommendedDocuments(@RequestBody List<Long> documentIds) {
        if (documentIds == null || documentIds.isEmpty()) {
            return Result.success(java.util.Collections.emptyList());
        }
        List<Document> documents = documentService.getDocumentsByIds(documentIds);
        return Result.success(documents);
    }
}