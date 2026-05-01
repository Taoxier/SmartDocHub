-- 为文档表添加索引，优化相似度检测查询
ALTER TABLE `doc_document` ADD INDEX `idx_parsed_content` (`parsed_content`(255));

-- 为文档向量表添加索引
ALTER TABLE `doc_document_vector` ADD INDEX `idx_vector` (`vector`(255));

-- 为内容块表添加索引，优化内容块相似度查询
ALTER TABLE `doc_content_chunk` ADD INDEX `idx_content_text` (`content_text`(255));

-- 为相似度表添加复合索引，优化相似度查询
ALTER TABLE `doc_similarity` ADD INDEX `idx_source_target` (`source_doc_id`, `target_doc_id`);

-- 为文档表添加索引，优化按相似度排序
ALTER TABLE `doc_document` ADD INDEX `idx_text_similarity` (`text_similarity`);
ALTER TABLE `doc_document` ADD INDEX `idx_table_similarity` (`table_similarity`);
ALTER TABLE `doc_document` ADD INDEX `idx_formula_similarity` (`formula_similarity`);