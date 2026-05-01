-- 创建评论表
CREATE TABLE `doc_comment` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `document_id` bigint NOT NULL COMMENT '文档ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '评论内容',
  `audit_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'PENDING' COMMENT '审核状态: PENDING, PASS, REJECT, NEED_REVIEW',
  `audit_confidence` decimal(5,2) DEFAULT '0.00' COMMENT '审核置信度',
  `risk_types` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '风险类型',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_document_id` (`document_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_audit_status` (`audit_status`),
  CONSTRAINT `doc_comment_ibfk_1` FOREIGN KEY (`document_id`) REFERENCES `doc_document` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文档评论表';

-- 创建敏感词库表
CREATE TABLE `doc_sensitive_word` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `word` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '敏感词',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类: PORN, VIOLENCE, POLITICS, AD, ILLEGAL',
  `level` int DEFAULT '1' COMMENT '级别: 1-警告, 2-拒绝',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_word` (`word`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='敏感词库表';