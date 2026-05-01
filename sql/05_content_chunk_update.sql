-- 修改内容分块表，添加标题和顺序字段
ALTER TABLE `doc_content_chunk` 
ADD COLUMN `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '块标题',
ADD COLUMN `order_index` int DEFAULT '0' COMMENT '顺序索引',
ADD COLUMN `preview_text` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '预览文本';

-- 添加顺序索引
CREATE INDEX `idx_order_index` ON `doc_content_chunk` (`order_index`);