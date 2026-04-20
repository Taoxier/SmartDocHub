package com.taoxier.smartdochub.document.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taoxier.smartdochub.common.result.PageResult;
import com.taoxier.smartdochub.common.result.Result;
import com.taoxier.smartdochub.document.model.entity.DocCategory;
import com.taoxier.smartdochub.document.service.DocCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "管理-分类管理")
@RestController
@RequestMapping("/api/admin/category")
@RequiredArgsConstructor
@Slf4j
public class AdminCategoryController {

    private final DocCategoryService docCategoryService;

    @GetMapping("/list")
    @Operation(summary = "获取所有分类")
    @PreAuthorize("@ss.hasPerm('doc:category:query')")
    public Result<List<DocCategory>> getAllCategories() {
        List<DocCategory> categories = docCategoryService.list(
                new LambdaQueryWrapper<DocCategory>()
                        .orderByAsc(DocCategory::getSort));
        return Result.success(categories);
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询分类")
    @PreAuthorize("@ss.hasPerm('doc:category:query')")
    public PageResult<DocCategory> queryCategoryPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<DocCategory> page = new Page<>(pageNum, pageSize);
        IPage<DocCategory> categoryPage = docCategoryService.page(page,
                new LambdaQueryWrapper<DocCategory>()
                        .orderByAsc(DocCategory::getSort));
        return PageResult.success(categoryPage);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取分类详情")
    @PreAuthorize("@ss.hasPerm('doc:category:query')")
    public Result<DocCategory> getCategory(@PathVariable Long id) {
        DocCategory category = docCategoryService.getById(id);
        return Result.success(category);
    }

    @PostMapping
    @Operation(summary = "创建分类")
    @PreAuthorize("@ss.hasPerm('doc:category:add')")
    public Result<DocCategory> createCategory(@RequestBody DocCategory category) {
        if (category.getSort() == null) {
            category.setSort(0);
        }
        if (category.getStatus() == null) {
            category.setStatus((byte) 1);
        }
        docCategoryService.save(category);
        return Result.success(category);
    }

    @PutMapping
    @Operation(summary = "更新分类")
    @PreAuthorize("@ss.hasPerm('doc:category:edit')")
    public Result<DocCategory> updateCategory(@RequestBody DocCategory category) {
        docCategoryService.updateById(category);
        return Result.success(category);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除分类")
    @PreAuthorize("@ss.hasPerm('doc:category:delete')")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        docCategoryService.removeById(id);
        return Result.success();
    }

    @PostMapping("/batch-delete")
    @Operation(summary = "批量删除分类")
    @PreAuthorize("@ss.hasPerm('doc:category:delete')")
    public Result<Void> batchDeleteCategories(@RequestBody List<Long> ids) {
        docCategoryService.removeByIds(ids);
        return Result.success();
    }
}
