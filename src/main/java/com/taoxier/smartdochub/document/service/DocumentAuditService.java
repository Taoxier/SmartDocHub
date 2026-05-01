package com.taoxier.smartdochub.document.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.taoxier.smartdochub.document.model.entity.DocumentAudit;

import java.util.List;

public interface DocumentAuditService extends IService<DocumentAudit> {

    Long submitAudit(Long documentId, String auditType, String storagePath);

    DocumentAudit getAuditStatus(Long documentId);

    void processAuditResult(Long auditId, String status, String result);

    List<DocumentAudit> listByDocumentId(Long documentId);
}