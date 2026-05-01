package com.taoxier.smartdochub.document.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.taoxier.smartdochub.document.model.entity.DocumentAudit;
import com.taoxier.smartdochub.document.service.DocumentAuditService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taoxier.smartdochub.document.mapper.DocumentAuditMapper;
import com.taoxier.smartdochub.ai.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DocumentAuditServiceImpl extends ServiceImpl<DocumentAuditMapper, DocumentAudit>
        implements DocumentAuditService {

    @Autowired
    private AIService aiService;

    @Override
    public Long submitAudit(Long documentId, String auditType, String storagePath) {
        DocumentAudit audit = new DocumentAudit();
        audit.setDocumentId(documentId);
        audit.setAuditType(auditType);
        audit.setStatus("PENDING");
        audit.setTextStatus("PENDING");
        audit.setImageStatus("PENDING");
        audit.setOverallStatus("PENDING");
        audit.setOperator("SYSTEM");

        save(audit);

        new Thread(() -> {
            try {
                String result = aiService.auditDocument(documentId, auditType, storagePath);

                if (result.contains("\"status\": \"PASS\"")) {
                    processAuditResult(audit.getId(), "PASS", result);
                } else if (result.contains("\"status\": \"REJECT\"")) {
                    processAuditResult(audit.getId(), "REJECT", result);
                } else {
                    processAuditResult(audit.getId(), "REJECT", "审核结果异常: " + result);
                }
            } catch (Exception e) {
                processAuditResult(audit.getId(), "REJECT", "审核失败: " + e.getMessage());
            }
        }).start();

        return audit.getId();
    }

    @Override
    public DocumentAudit getAuditStatus(Long documentId) {
        return getOne(new LambdaQueryWrapper<DocumentAudit>()
                .eq(DocumentAudit::getDocumentId, documentId)
                .orderByDesc(DocumentAudit::getId)
                .last("LIMIT 1"));
    }

    @Override
    public void processAuditResult(Long auditId, String status, String result) {
        DocumentAudit audit = getById(auditId);
        if (audit != null) {
            audit.setOverallStatus(status);
            audit.setTextStatus(status);
            audit.setStatus(status);
            if ("TEXT".equals(audit.getAuditType()) || "ALL".equals(audit.getAuditType())) {
                audit.setTextDetails(result);
            }
            if ("IMAGE".equals(audit.getAuditType()) || "ALL".equals(audit.getAuditType())) {
                audit.setImageDetails(result);
            }
            audit.setAuditTime(LocalDateTime.now());
            updateById(audit);
        }
    }

    @Override
    public List<DocumentAudit> listByDocumentId(Long documentId) {
        return list(new LambdaQueryWrapper<DocumentAudit>()
                .eq(DocumentAudit::getDocumentId, documentId)
                .orderByDesc(DocumentAudit::getId));
    }
}
