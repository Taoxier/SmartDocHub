-- 创建文档审核记录表
CREATE TABLE `doc_document_audit` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '审核ID',
  `document_id` bigint NOT NULL COMMENT '文档ID',
  `audit_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '审核类型: TEXT, IMAGE, ALL',
  `text_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'PENDING' COMMENT '文本审核状态: PENDING, PASS, WARN, REJECT',
  `image_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'PENDING' COMMENT '图片审核状态: PENDING, PASS, WARN, REJECT',
  `overall_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'PENDING' COMMENT '总体状态: PENDING, PASS, WARN, REJECT',
  `text_details` json DEFAULT NULL COMMENT '文本审核详情',
  `image_details` json DEFAULT NULL COMMENT '图片审核详情',
  `audit_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '审核时间',
  `operator` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '操作人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_document_id` (`document_id`),
  CONSTRAINT `doc_document_audit_ibfk_1` FOREIGN KEY (`document_id`) REFERENCES `doc_document` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文档审核记录表';