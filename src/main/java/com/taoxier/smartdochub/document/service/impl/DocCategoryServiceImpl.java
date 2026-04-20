package com.taoxier.smartdochub.document.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taoxier.smartdochub.document.mapper.DocCategoryMapper;
import com.taoxier.smartdochub.document.mapper.DocDocumentCategoryMapper;
import com.taoxier.smartdochub.document.model.entity.DocCategory;
import com.taoxier.smartdochub.document.model.entity.DocDocumentCategory;
import com.taoxier.smartdochub.document.service.DocCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocCategoryServiceImpl extends ServiceImpl<DocCategoryMapper, DocCategory> implements DocCategoryService {

    private final DocDocumentCategoryMapper docDocumentCategoryMapper;

    @Override
    public List<DocCategory> getAllCategories() {
        return this.list(
                new LambdaQueryWrapper<DocCategory>()
                        .eq(DocCategory::getStatus, 1)
                        .orderByAsc(DocCategory::getSort)
        );
    }

    @Override
    public List<DocCategory> getChildCategories(Long parentId) {
        return this.list(
                new LambdaQueryWrapper<DocCategory>()
                        .eq(DocCategory::getParentId, parentId)
                        .eq(DocCategory::getStatus, 1)
                        .orderByAsc(DocCategory::getSort)
        );
    }

    @Override
    public void addCategory(DocCategory category) {
        this.save(category);
    }

    @Override
    public void updateCategory(DocCategory category) {
        this.updateById(category);
    }

    @Override
    public void deleteCategory(Long id) {
        this.removeById(id);
    }

    @Override
    public List<Long> getDocumentCategories(Long documentId) {
        List<DocDocumentCategory> relations = docDocumentCategoryMapper.selectList(
                new LambdaQueryWrapper<DocDocumentCategory>()
                        .eq(DocDocumentCategory::getDocumentId, documentId)
        );
        return relations.stream()
                .map(DocDocumentCategory::getCategoryId)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void setDocumentCategories(Long documentId, List<Long> categoryIds) {
        // 先删除原有的关联
        docDocumentCategoryMapper.delete(
                new LambdaQueryWrapper<DocDocumentCategory>()
                        .eq(DocDocumentCategory::getDocumentId, documentId)
        );
        
        // 添加新的关联
        for (Long categoryId : categoryIds) {
            DocDocumentCategory relation = new DocDocumentCategory();
            relation.setDocumentId(documentId);
            relation.setCategoryId(categoryId);
            docDocumentCategoryMapper.insert(relation);
        }
    }

}
