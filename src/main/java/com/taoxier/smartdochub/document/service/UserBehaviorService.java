package com.taoxier.smartdochub.document.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.taoxier.smartdochub.document.model.entity.UserBehavior;

public interface UserBehaviorService extends IService<UserBehavior> {

    void recordBehavior(Long userId, Long documentId, String behaviorType);

    void recordBehavior(Long userId, Long documentId, String behaviorType, Integer durationSeconds);

    void recordRating(Long userId, Long documentId, Byte qualityRating, Byte readabilityRating);

    void recordSearch(Long userId, String searchQuery);

    int countByDocumentAndType(Long documentId, String behaviorType);

    void removeByUserAndDocumentAndType(Long userId, Long documentId, String behaviorType);

    /**
     * 根据用户ID和行为类型获取文档ID列表
     *
     * @param userId       用户ID
     * @param behaviorType 行为类型
     * @return 文档ID列表
     */
    java.util.List<Long> getDocumentIdsByUserAndType(Long userId, String behaviorType);

    /**
     * 批量删除用户的行为记录
     *
     * @param userId       用户ID
     * @param documentIds  文档ID列表
     * @param behaviorType 行为类型
     */
    void batchRemoveByUserAndDocumentAndType(Long userId, java.util.List<Long> documentIds, String behaviorType);

    /**
     * 删除用户的所有指定类型的行为记录
     *
     * @param userId       用户ID
     * @param behaviorType 行为类型
     */
    void removeAllByUserAndType(Long userId, String behaviorType);

    /**
     * 检查用户是否已收藏指定文档
     *
     * @param userId     用户ID
     * @param documentId 文档ID
     * @return 是否已收藏
     */
    boolean hasFavorited(Long userId, Long documentId);

    /**
     * 计算并更新文档的平均评分
     *
     * @param documentId 文档ID
     * @return 平均评分
     */
    java.math.BigDecimal calculateAndUpdateAvgRating(Long documentId);

    /**
     * 计算文档的质量评分和可读性评分
     *
     * @param documentId 文档ID
     * @return 包含质量评分和可读性评分的Map
     */
    java.util.Map<String, java.math.BigDecimal> calculateDocumentScores(Long documentId);
}
