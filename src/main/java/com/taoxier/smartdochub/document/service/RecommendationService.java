package com.taoxier.smartdochub.document.service;

import java.util.List;

public interface RecommendationService {
    /**
     * 获取用户的推荐文档列表
     * 
     * @param userId 用户ID
     * @param limit  推荐数量
     * @return 推荐的文档ID列表
     */
    List<Long> getRecommendations(Long userId, int limit);

    /**
     * 获取用户的推荐文档列表（强制刷新）
     * 
     * @param userId  用户ID
     * @param limit   推荐数量
     * @param refresh 是否强制刷新
     * @return 推荐的文档ID列表
     */
    List<Long> getRecommendations(Long userId, int limit, boolean refresh);

    /**
     * 获取用户的推荐文档列表（带offset）
     * 
     * @param userId  用户ID
     * @param limit   推荐数量
     * @param offset  偏移量
     * @param refresh 是否强制刷新
     * @return 推荐的文档ID列表
     */
    List<Long> getRecommendations(Long userId, int limit, int offset, boolean refresh);
}
