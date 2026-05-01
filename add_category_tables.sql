-- 添加文档分类表
CREATE TABLE IF NOT EXISTS `doc_category` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类名称',
  `parent_id` bigint DEFAULT '0' COMMENT '父分类ID',
  `sort` int DEFAULT '0' COMMENT '排序',
  `status` tinyint DEFAULT '1' COMMENT '状态(1-正常 0-禁用)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文档分类表';

-- 添加文档-分类关联表
CREATE TABLE IF NOT EXISTS `doc_document_category` (
  `document_id` bigint NOT NULL COMMENT '文档ID',
  `category_id` bigint NOT NULL COMMENT '分类ID',
  PRIMARY KEY (`document_id`,`category_id`),
  KEY `idx_category_id` (`category_id`),
  CONSTRAINT `doc_document_category_ibfk_1` FOREIGN KEY (`document_id`) REFERENCES `doc_document` (`id`) ON DELETE CASCADE,
  CONSTRAINT `doc_document_category_ibfk_2` FOREIGN KEY (`category_id`) REFERENCES `doc_category` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文档-分类关联表';

-- 插入默认分类
INSERT INTO `doc_category` (`name`, `parent_id`, `sort`, `status`) VALUES
('技术文档', 0, 1, 1),
('学术论文', 0, 2, 1),
('工作报告', 0, 3, 1),
('其他文档', 0, 4, 1);
