-- 创建AI分析结果表
CREATE TABLE `doc_ai_analysis` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分析ID',
  `document_id` bigint NOT NULL COMMENT '文档ID',
  `ai_probability` decimal(5,2) DEFAULT '0.00' COMMENT 'AI生成概率（0-100）',
  `confidence` decimal(5,2) DEFAULT '0.00' COMMENT '置信度（0-100）',
  `detected_model` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '检测到的AI模型类型',
  `key_features` json DEFAULT NULL COMMENT '关键特征段落（JSON格式）',
  `result` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'PASS' COMMENT '分析结果（PASS, WARN, REJECT）',
  `analysis_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '分析时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_document_id` (`document_id`),
  CONSTRAINT `doc_ai_analysis_ibfk_1` FOREIGN KEY (`document_id`) REFERENCES `doc_document` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI分析结果表';