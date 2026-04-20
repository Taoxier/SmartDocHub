package com.taoxier.smartdochub.document.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.taoxier.smartdochub.document.model.entity.DocCategory;

import java.util.List;

public interface DocCategoryService extends IService<DocCategory> {

    List<DocCategory> getAllCategories();

    List<DocCategory> getChildCategories(Long parentId);

    void addCategory(DocCategory category);

    void updateCategory(DocCategory category);

    void deleteCategory(Long id);

    List<Long> getDocumentCategories(Long documentId);

    void setDocumentCategories(Long documentId, List<Long> categoryIds);

}
