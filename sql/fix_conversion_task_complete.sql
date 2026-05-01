-- =====================================================
-- SmartDocHub 修复格式转换任务表 SQL
-- 执行时间：2026-04-23
-- 说明：添加所有缺失的字段
-- =====================================================

-- 为doc_conversion_task表添加缺失的字段

-- 1. 添加user_id字段
ALTER TABLE doc_conversion_task ADD COLUMN user_id BIGINT COMMENT '用户ID';

-- 2. 添加progress字段
ALTER TABLE doc_conversion_task ADD COLUMN progress INT DEFAULT 0 COMMENT '任务进度(0-100)';

-- 3. 添加result_path字段
ALTER TABLE doc_conversion_task ADD COLUMN result_path VARCHAR(500) COMMENT '转换结果文件路径';

-- 4. 添加error_message字段
ALTER TABLE doc_conversion_task ADD COLUMN error_message VARCHAR(1000) COMMENT '错误信息';
