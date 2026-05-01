-- 修复文档审核表字段缺失问题
-- 添加缺失的字段以匹配Java实体类

-- 检查并添加字段
ALTER TABLE `doc_document_audit` 
ADD COLUMN IF NOT EXISTS `result` TEXT COMMENT '审核结果（JSON格式）' AFTER `status`,
ADD COLUMN IF NOT EXISTS `auditor` varchar(50) DEFAULT NULL COMMENT '审核人（系统或AI）' AFTER `audit_time`,
ADD COLUMN IF NOT EXISTS `remark` varchar(500) DEFAULT NULL COMMENT '备注' AFTER `auditor`,
ADD COLUMN IF NOT EXISTS `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间' AFTER `remark`,
ADD COLUMN IF NOT EXISTS `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `create_time`;

-- 显示表结构
DESC `doc_document_audit`;
