package com.taoxier.smartdochub.document.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taoxier.smartdochub.document.model.entity.DocumentAudit;

/**
 * 文档审核Mapper
 */
public interface DocumentAuditMapper extends BaseMapper<DocumentAudit> {

    /**
     * 根据文档ID获取最新审核记录
     * @param documentId 文档ID
     * @return 审核记录
     */
    DocumentAudit selectLatestByDocumentId(Long documentId);
}