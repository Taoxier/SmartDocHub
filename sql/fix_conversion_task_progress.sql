-- =====================================================
-- SmartDocHub 修复格式转换任务表 SQL
-- 执行时间：2026-04-23
-- 说明：添加缺失的progress字段
-- =====================================================

-- 为doc_conversion_task表添加progress字段
ALTER TABLE doc_conversion_task ADD COLUMN progress INT DEFAULT 0 COMMENT '任务进度(0-100)';
