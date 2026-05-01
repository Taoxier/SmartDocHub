-- =====================================================
-- SmartDocHub 敏感词数据
-- 执行时间：2026-04-23
-- 说明：插入常见的敏感词
-- =====================================================

-- 插入政治类敏感词
INSERT INTO doc_sensitive_word (word, category, level, create_time) VALUES
('政治敏感词1', 'POLITICS', 2, NOW()),
('政治敏感词2', 'POLITICS', 2, NOW()),
('政治敏感词3', 'POLITICS', 2, NOW()),
('政治敏感词4', 'POLITICS', 2, NOW()),
('政治敏感词5', 'POLITICS', 2, NOW());

-- 插入色情类敏感词
INSERT INTO doc_sensitive_word (word, category, level, create_time) VALUES
('色情词汇1', 'PORN', 2, NOW()),
('色情词汇2', 'PORN', 2, NOW()),
('色情词汇3', 'PORN', 2, NOW()),
('色情词汇4', 'PORN', 2, NOW()),
('色情词汇5', 'PORN', 2, NOW());

-- 插入暴力类敏感词
INSERT INTO doc_sensitive_word (word, category, level, create_time) VALUES
('暴力词汇1', 'VIOLENCE', 1, NOW()),
('暴力词汇2', 'VIOLENCE', 1, NOW()),
('暴力词汇3', 'VIOLENCE', 1, NOW()),
('暴力词汇4', 'VIOLENCE', 1, NOW()),
('暴力词汇5', 'VIOLENCE', 1, NOW());

-- 插入广告类敏感词
INSERT INTO doc_sensitive_word (word, category, level, create_time) VALUES
('广告词汇1', 'AD', 1, NOW()),
('广告词汇2', 'AD', 1, NOW()),
('广告词汇3', 'AD', 1, NOW()),
('广告词汇4', 'AD', 1, NOW()),
('广告词汇5', 'AD', 1, NOW());

-- 插入违法类敏感词
INSERT INTO doc_sensitive_word (word, category, level, create_time) VALUES
('违法词汇1', 'ILLEGAL', 2, NOW()),
('违法词汇2', 'ILLEGAL', 2, NOW()),
('违法词汇3', 'ILLEGAL', 2, NOW()),
('违法词汇4', 'ILLEGAL', 2, NOW()),
('违法词汇5', 'ILLEGAL', 2, NOW());
