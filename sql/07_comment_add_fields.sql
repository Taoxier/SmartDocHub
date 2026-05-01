-- 添加评论表缺失的字段
ALTER TABLE `doc_comment` 
ADD COLUMN `parent_id` bigint DEFAULT NULL COMMENT '父评论ID（用于回复）',
ADD COLUMN `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
ADD COLUMN `audit_reason` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '审核原因',
ADD COLUMN `like_count` int DEFAULT 0 COMMENT '点赞数',
ADD COLUMN `status` tinyint DEFAULT 0 COMMENT '状态（0-正常，1-删除）';

-- 添加索引
ALTER TABLE `doc_comment` ADD INDEX `idx_parent_id` (`parent_id`);
