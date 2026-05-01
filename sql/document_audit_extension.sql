-- 文档审核功能扩展
-- 为 doc_document 表添加审核状态和审核结果字段

-- 添加审核状态字段
ALTER TABLE doc_document ADD COLUMN audit_status VARCHAR(20) DEFAULT 'PENDING' COMMENT '审核状态: PENDING(待审核), AUDITING(审核中), APPROVED(通过), REJECTED(拒绝)';

-- 添加审核结果信息字段
ALTER TABLE doc_document ADD COLUMN audit_result TEXT COMMENT '审核结果信息';

-- 为已存在的文档设置默认审核状态为通过（因为它们是在审核功能添加前上传的）
UPDATE doc_document SET audit_status = 'APPROVED' WHERE audit_status IS NULL OR audit_status = '';

-- 可选：添加索引以加快审核状态查询
-- ALTER TABLE doc_document ADD INDEX idx_audit_status (audit_status);
