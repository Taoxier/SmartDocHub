package com.taoxier.smartdochub.document.service.impl;

import com.taoxier.smartdochub.document.service.DocumentService;
import com.taoxier.smartdochub.document.service.RecommendationService;
import com.taoxier.smartdochub.document.service.UserBehaviorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RecommendationServiceImpl implements RecommendationService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private UserBehaviorService userBehaviorService;

    @Override
    public List<Long> getRecommendations(Long userId, int limit) {
        return getRecommendations(userId, limit, 0, false);
    }

    @Override
    public List<Long> getRecommendations(Long userId, int limit, boolean refresh) {
        return getRecommendations(userId, limit, 0, refresh);
    }

    @Override
    public List<Long> getRecommendations(Long userId, int limit, int offset, boolean refresh) {
        try {
            String key = "recommend:user:" + userId;

            // 如果是刷新，生成新的随机推荐并覆盖缓存
            if (refresh) {
                log.info("Refresh requested, generating new random recommendations for user: {}", userId);
                List<Long> newRecommendations = generateRecommendationsBasedOnBehavior(userId, limit);
                if (newRecommendations.isEmpty()) {
                    newRecommendations = documentService.getRandomHotDocumentIds(limit, 20);
                }
                // 覆盖缓存
                redisTemplate.delete(key);
                for (Long docId : newRecommendations) {
                    redisTemplate.opsForList().rightPush(key, docId.toString());
                }
                redisTemplate.expire(key, java.time.Duration.ofDays(7));
                return newRecommendations;
            }

            // 先查Redis全部缓存
            List<String> allDocIdStrings = redisTemplate.opsForList().range(key, 0, -1);

            if (allDocIdStrings != null && !allDocIdStrings.isEmpty()) {
                // 如果有缓存，根据offset和limit返回
                if (offset < allDocIdStrings.size()) {
                    int endIndex = Math.min(offset + limit, allDocIdStrings.size());
                    List<String> pageDocIdStrings = allDocIdStrings.subList(offset, endIndex);
                    return pageDocIdStrings.stream()
                            .map(s -> {
                                try {
                                    return Long.parseLong(s);
                                } catch (NumberFormatException e) {
                                    return null;
                                }
                            })
                            .filter(id -> id != null)
                            .collect(Collectors.toList());
                }
                return new ArrayList<>();
            }

            // 如果 Redis 中没有推荐数据，尝试基于用户行为生成推荐
            log.info("No recommendations found in Redis, generating based on user behavior");
            List<Long> behaviorBasedRecommendations = generateRecommendationsBasedOnBehavior(userId, limit);
            if (!behaviorBasedRecommendations.isEmpty()) {
                // 保存到Redis
                redisTemplate.delete(key);
                for (Long docId : behaviorBasedRecommendations) {
                    redisTemplate.opsForList().rightPush(key, docId.toString());
                }
                redisTemplate.expire(key, java.time.Duration.ofDays(7));
                return behaviorBasedRecommendations;
            }

            // 如果没有用户行为数据，返回热门文档作为默认推荐
            log.info("No user behavior data found, returning default hot documents");
            return documentService.getRandomHotDocumentIds(limit, 20);
        } catch (Exception e) {
            log.error("获取推荐列表失败", e);
            // 发生异常时返回热门文档
            try {
                return documentService.getRandomHotDocumentIds(limit, 20);
            } catch (Exception ex) {
                log.error("获取热门文档失败", ex);
                return new ArrayList<>();
            }
        }
    }

    /**
     * 基于用户行为生成推荐
     *
     * @param userId 用户ID
     * @param limit  推荐数量
     * @return 推荐的文档ID列表
     */
    private List<Long> generateRecommendationsBasedOnBehavior(Long userId, int limit) {
        try {
            // 获取用户的浏览历史
            List<Long> viewedDocumentIds = userBehaviorService.getDocumentIdsByUserAndType(userId, "VIEW");

            // 获取用户的下载历史
            List<Long> downloadedDocumentIds = userBehaviorService.getDocumentIdsByUserAndType(userId, "DOWNLOAD");

            // 获取用户的收藏
            List<Long> favoritedDocumentIds = userBehaviorService.getDocumentIdsByUserAndType(userId, "FAVORITE");

            // 合并所有用户交互的文档
            List<Long> allInteractedDocuments = new ArrayList<>();
            allInteractedDocuments.addAll(viewedDocumentIds);
            allInteractedDocuments.addAll(downloadedDocumentIds);
            allInteractedDocuments.addAll(favoritedDocumentIds);

            // 去重
            List<Long> uniqueInteractedDocuments = allInteractedDocuments.stream().distinct()
                    .collect(Collectors.toList());

            if (uniqueInteractedDocuments.isEmpty()) {
                return new ArrayList<>();
            }

            // 获取与这些文档类型相似的文档
            // 使用随机偏移获取不同的热门文档
            List<Long> hotDocumentIds = documentService.getRandomHotDocumentIds(limit * 2, 30);
            List<Long> recommendations = new ArrayList<>();

            for (Long hotDocId : hotDocumentIds) {
                if (!uniqueInteractedDocuments.contains(hotDocId) && recommendations.size() < limit) {
                    recommendations.add(hotDocId);
                }
            }

            return recommendations;
        } catch (Exception e) {
            log.error("基于用户行为生成推荐失败", e);
            return new ArrayList<>();
        }
    }
}
