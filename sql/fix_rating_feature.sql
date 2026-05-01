-- =====================================================
-- SmartDocHub 评分功能修复 SQL
-- 执行时间：2026-04-23
-- 说明：修复评分功能，添加必要的字段
-- =====================================================

-- 1. 在 doc_document 表添加 avg_rating 字段（平均评分）
ALTER TABLE doc_document ADD COLUMN IF NOT EXISTS avg_rating DECIMAL(3,2) DEFAULT NULL COMMENT '用户平均评分(1-5分)';

-- 2. 在 user_behavior 表添加质量评分和可读性评分字段
ALTER TABLE user_behavior ADD COLUMN IF NOT EXISTS quality_rating TINYINT DEFAULT NULL COMMENT '质量评分(1-5分)' AFTER rating_value;
ALTER TABLE user_behavior ADD COLUMN IF NOT EXISTS readability_rating TINYINT DEFAULT NULL COMMENT '可读性评分(1-5分)' AFTER quality_rating;

-- 3. 创建存储过程来计算并更新文档的平均评分
DELIMITER //
CREATE PROCEDURE IF NOT EXISTS update_document_avg_rating(IN doc_id BIGINT)
BEGIN
    DECLARE avg_val DECIMAL(3,2);

    SELECT COALESCE(AVG((COALESCE(quality_rating, 0) + COALESCE(readability_rating, 0)) / 2.0), 0)
    INTO avg_val
    FROM user_behavior
    WHERE document_id = doc_id
      AND behavior_type = 'RATE'
      AND (quality_rating IS NOT NULL OR readability_rating IS NOT NULL);

    UPDATE doc_document SET avg_rating = avg_val WHERE id = doc_id;
END //
DELIMITER ;

-- 4. 创建触发器，在用户评分后自动更新文档的平均评分
DELIMITER //
CREATE TRIGGER IF NOT EXISTS after_rating_insert
AFTER INSERT ON user_behavior
FOR EACH ROW
BEGIN
    IF NEW.behavior_type = 'RATE' AND (NEW.quality_rating IS NOT NULL OR NEW.readability_rating IS NOT NULL) THEN
        CALL update_document_avg_rating(NEW.document_id);
    END IF;
END //
DELIMITER ;

-- 5. 如果触发器已存在，则删除后重新创建
-- （如果上面的创建失败，可能需要手动执行以下语句）
-- DROP TRIGGER IF EXISTS after_rating_insert;
