-- 数据库升级脚本：添加文档版本化支持

-- 1. 创建文档版本表
DROP TABLE IF EXISTS `doc_document_version`;
CREATE TABLE `doc_document_version` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '版本ID',
  `document_id` bigint NOT NULL COMMENT '文档ID',
  `version_number` int NOT NULL COMMENT '版本号',
  `title` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文档标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '文档描述',
  `category` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '文档分类',
  `original_filename` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '原始文件名',
  `storage_path` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '存储路径',
  `file_size` bigint NOT NULL COMMENT '文件大小(字节)',
  `file_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文件类型',
  `file_hash` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '文件哈希值，用于去重',
  `upload_user_id` bigint NOT NULL COMMENT '上传用户ID',
  `process_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'UPLOADED' COMMENT '处理状态: UPLOADED, PARSING, SIMILARITY_CHECKING, AI_DETECTING, COMPLETED, FAILED',
  `process_progress` int DEFAULT '0' COMMENT '处理进度(0-100)',
  `process_message` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '处理状态信息',
  `page_count` int DEFAULT '0' COMMENT '总页数',
  `word_count` int DEFAULT '0' COMMENT '总字数',
  `character_count` int DEFAULT '0' COMMENT '总字符数',
  `parsed_content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '解析后的完整文本（用于搜索）',
  `overall_similarity` decimal(5,4) DEFAULT '0.0000' COMMENT '总体重复率',
  `text_similarity` decimal(5,4) DEFAULT '0.0000' COMMENT '文字重复率',
  `table_similarity` decimal(5,4) DEFAULT '0.0000' COMMENT '表格重复率',
  `formula_similarity` decimal(5,4) DEFAULT '0.0000' COMMENT '公式重复率',
  `ai_probability` decimal(5,4) DEFAULT '0.0000' COMMENT 'AI生成总体概率',
  `detected_ai_model` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '检测到的AI模型类型',
  `quality_score` decimal(5,4) DEFAULT '0.0000' COMMENT '质量评分',
  `readability_score` decimal(5,4) DEFAULT '0.0000' COMMENT '可读性评分',
  `is_duplicate` tinyint DEFAULT '0' COMMENT '是否重复',
  `is_ai_generated` tinyint DEFAULT '0' COMMENT '是否AI生成',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_doc_version` (`document_id`,`version_number`),
  KEY `idx_document_id` (`document_id`),
  KEY `idx_upload_user` (`upload_user_id`),
  KEY `idx_process_status` (`process_status`),
  FULLTEXT KEY `idx_version_search` (`title`,`description`,`parsed_content`),
  CONSTRAINT `doc_document_version_ibfk_1` FOREIGN KEY (`document_id`) REFERENCES `doc_document` (`id`) ON DELETE CASCADE,
  CONSTRAINT `doc_document_version_ibfk_2` FOREIGN KEY (`upload_user_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文档版本表';

-- 2. 修改文档主表，添加版本相关字段和分类字段
ALTER TABLE `doc_document` ADD COLUMN `current_version` int DEFAULT '1' COMMENT '当前版本号' AFTER `is_deleted`;
ALTER TABLE `doc_document` ADD COLUMN `version_count` int DEFAULT '1' COMMENT '版本总数' AFTER `current_version`;
ALTER TABLE `doc_document` ADD COLUMN `category` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '文档分类' AFTER `description`;

-- 3. 修改内容分块表，添加版本号字段
ALTER TABLE `doc_content_chunk` ADD COLUMN `version_number` int DEFAULT '1' COMMENT '版本号' AFTER `document_id`;
ALTER TABLE `doc_content_chunk` ADD KEY `idx_doc_version` (`document_id`,`version_number`);

-- 4. 修改主题表，添加版本号字段
ALTER TABLE `doc_topic` ADD COLUMN `version_number` int DEFAULT '1' COMMENT '版本号' AFTER `document_id`;
ALTER TABLE `doc_topic` ADD KEY `idx_doc_version` (`document_id`,`version_number`);

-- 5. 创建异步任务表
DROP TABLE IF EXISTS `doc_async_task`;
CREATE TABLE `doc_async_task` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `task_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务类型: DOCUMENT_PARSE, SIMILARITY_CHECK, AI_DETECTION, KEYWORD_EXTRACTION',
  `document_id` bigint NOT NULL COMMENT '文档ID',
  `version_number` int NOT NULL COMMENT '版本号',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'PENDING' COMMENT '任务状态: PENDING, RUNNING, COMPLETED, FAILED',
  `progress` int DEFAULT '0' COMMENT '任务进度(0-100)',
  `result` json DEFAULT NULL COMMENT '任务结果',
  `error_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '错误信息',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  PRIMARY KEY (`id`),
  KEY `idx_document_id` (`document_id`),
  KEY `idx_status` (`status`),
  KEY `idx_task_type` (`task_type`),
  CONSTRAINT `doc_async_task_ibfk_1` FOREIGN KEY (`document_id`) REFERENCES `doc_document` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='异步任务表';

-- 6. 为现有文档创建初始版本记录
-- 注意：这部分需要根据实际数据执行，这里只提供示例SQL
-- INSERT INTO doc_document_version (document_id, version_number, title, description, original_filename, storage_path, file_size, file_type, file_hash, upload_user_id, process_status, process_progress, process_message, page_count, word_count, character_count, parsed_content, overall_similarity, text_similarity, table_similarity, formula_similarity, ai_probability, detected_ai_model, quality_score, readability_score, is_duplicate, is_ai_generated) SELECT id, 1, title, description, original_filename, storage_path, file_size, file_type, file_hash, upload_user_id, process_status, process_progress, process_message, page_count, word_count, character_count, parsed_content, overall_similarity, text_similarity, table_similarity, formula_similarity, ai_probability, detected_ai_model, quality_score, readability_score, is_duplicate, is_ai_generated FROM doc_document;

-- 7. 更新文档主表的版本信息
-- UPDATE doc_document SET current_version = 1, version_count = 1;

-- 8. 更新内容分块表的版本号
-- UPDATE doc_content_chunk SET version_number = 1;

-- 9. 更新主题表的版本号
-- UPDATE doc_topic SET version_number = 1;
