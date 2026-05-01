package com.taoxier.smartdochub.document.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.taoxier.smartdochub.document.model.entity.DocumentVector;

/**
 * 文档向量服务
 */
public interface DocumentVectorService extends IService<DocumentVector> {

    /**
     * 保存或更新文档向量
     * @param documentId 文档ID
     * @param vector 向量（JSON格式）
     */
    void saveOrUpdateVector(Long documentId, String vector);

    /**
     * 根据文档ID获取向量
     * @param documentId 文档ID
     * @return 文档向量
     */
    DocumentVector getByDocumentId(Long documentId);
}