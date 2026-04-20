package com.taoxier.smartdochub.document.controller;

import com.taoxier.smartdochub.common.result.Result;
import com.taoxier.smartdochub.document.model.entity.DocCategory;
import com.taoxier.smartdochub.document.service.DocCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "分类管理")
@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
public class CategoryController {

    private final DocCategoryService docCategoryService;

    @GetMapping
    @Operation(summary = "获取所有分类")
    public Result<List<DocCategory>> getAllCategories() {
        List<DocCategory> categories = docCategoryService.getAllCategories();
        return Result.success(categories);
    }

    @GetMapping("/child/{parentId}")
    @Operation(summary = "获取子分类")
    public Result<List<DocCategory>> getChildCategories(
            @Parameter(description = "父分类ID") @PathVariable Long parentId) {
        List<DocCategory> categories = docCategoryService.getChildCategories(parentId);
        return Result.success(categories);
    }

    @PostMapping
    @Operation(summary = "添加分类")
    public Result<Void> addCategory(@RequestBody DocCategory category) {
        docCategoryService.addCategory(category);
        return Result.success();
    }

    @PutMapping
    @Operation(summary = "更新分类")
    public Result<Void> updateCategory(@RequestBody DocCategory category) {
        docCategoryService.updateCategory(category);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除分类")
    public Result<Void> deleteCategory(
            @Parameter(description = "分类ID") @PathVariable Long id) {
        docCategoryService.deleteCategory(id);
        return Result.success();
    }

    @GetMapping("/document/{documentId}")
    @Operation(summary = "获取文档的分类")
    public Result<List<Long>> getDocumentCategories(
            @Parameter(description = "文档ID") @PathVariable Long documentId) {
        List<Long> categoryIds = docCategoryService.getDocumentCategories(documentId);
        return Result.success(categoryIds);
    }

    @PostMapping("/document/{documentId}")
    @Operation(summary = "设置文档的分类")
    public Result<Void> setDocumentCategories(
            @Parameter(description = "文档ID") @PathVariable Long documentId,
            @Parameter(description = "分类ID列表") @RequestBody List<Long> categoryIds) {
        docCategoryService.setDocumentCategories(documentId, categoryIds);
        return Result.success();
    }

}
