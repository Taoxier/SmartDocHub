package com.taoxier.smartdochub.document.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.taoxier.smartdochub.document.mapper.DocumentVectorMapper;
import com.taoxier.smartdochub.document.model.entity.DocumentVector;
import com.taoxier.smartdochub.document.service.DocumentVectorService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 文档向量服务实现
 */
@Service
public class DocumentVectorServiceImpl extends ServiceImpl<DocumentVectorMapper, DocumentVector> implements DocumentVectorService {

    @Override
    public void saveOrUpdateVector(Long documentId, String vector) {
        DocumentVector existingVector = getByDocumentId(documentId);
        if (existingVector != null) {
            existingVector.setVector(vector);
            updateById(existingVector);
        } else {
            DocumentVector newVector = new DocumentVector();
            newVector.setDocumentId(documentId);
            newVector.setVector(vector);
            newVector.setModelVersion("v1");
            save(newVector);
        }
    }

    @Override
    public DocumentVector getByDocumentId(Long documentId) {
        return getOne(new LambdaQueryWrapper<DocumentVector>()
                .eq(DocumentVector::getDocumentId, documentId));
    }
}