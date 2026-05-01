-- =====================================================
-- SmartDocHub 修复格式转换任务表 SQL
-- 执行时间：2026-04-23
-- 说明：添加缺失的user_id字段
-- =====================================================

-- 为doc_conversion_task表添加user_id字段
ALTER TABLE doc_conversion_task ADD COLUMN user_id BIGINT COMMENT '用户ID';
