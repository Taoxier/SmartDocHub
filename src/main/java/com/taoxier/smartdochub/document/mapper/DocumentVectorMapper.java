package com.taoxier.smartdochub.document.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taoxier.smartdochub.document.model.entity.DocumentVector;

/**
 * 文档向量Mapper
 */
public interface DocumentVectorMapper extends BaseMapper<DocumentVector> {

    /**
     * 根据文档ID获取向量
     * @param documentId 文档ID
     * @return 文档向量
     */
    DocumentVector selectByDocumentId(Long documentId);
}