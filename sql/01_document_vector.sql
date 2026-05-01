-- 创建文档向量表
CREATE TABLE `doc_document_vector` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `document_id` bigint NOT NULL COMMENT '文档ID',
  `vector` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '语义向量（JSON格式）',
  `model_version` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'v1' COMMENT '模型版本',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_document_id` (`document_id`),
  CONSTRAINT `doc_document_vector_ibfk_1` FOREIGN KEY (`document_id`) REFERENCES `doc_document` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文档语义向量表';