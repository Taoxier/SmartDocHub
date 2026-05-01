package com.taoxier.smartdochub.document.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taoxier.smartdochub.document.mapper.UserBehaviorMapper;
import com.taoxier.smartdochub.document.model.entity.UserBehavior;
import com.taoxier.smartdochub.document.service.UserBehaviorService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class UserBehaviorServiceImpl extends ServiceImpl<UserBehaviorMapper, UserBehavior>
        implements UserBehaviorService {

    @Override
    public void recordBehavior(Long userId, Long documentId, String behaviorType) {
        recordBehavior(userId, documentId, behaviorType, null);
    }

    @Override
    public void recordBehavior(Long userId, Long documentId, String behaviorType, Integer durationSeconds) {
        UserBehavior behavior = new UserBehavior();
        behavior.setUserId(userId);
        behavior.setDocumentId(documentId);
        behavior.setBehaviorType(behaviorType);
        behavior.setBehaviorIntensity(BigDecimal.ONE);
        behavior.setDurationSeconds(durationSeconds != null ? durationSeconds : 0);
        behavior.setCreateTime(LocalDateTime.now());
        this.save(behavior);
    }

    @Override
    public void recordRating(Long userId, Long documentId, Byte qualityRating, Byte readabilityRating) {
        UserBehavior behavior = new UserBehavior();
        behavior.setUserId(userId);
        behavior.setDocumentId(documentId);
        behavior.setBehaviorType("RATE");
        behavior.setBehaviorIntensity(BigDecimal.ONE);
        behavior.setQualityRating(qualityRating);
        behavior.setReadabilityRating(readabilityRating);
        behavior.setCreateTime(LocalDateTime.now());
        this.save(behavior);
    }

    @Override
    public BigDecimal calculateAndUpdateAvgRating(Long documentId) {
        if (documentId == null) {
            return BigDecimal.ZERO;
        }
        java.util.List<UserBehavior> ratings = this.list(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserBehavior>()
                        .eq(UserBehavior::getDocumentId, documentId)
                        .eq(UserBehavior::getBehaviorType, "RATE")
                        .and(w -> w.isNotNull(UserBehavior::getQualityRating)
                                .or()
                                .isNotNull(UserBehavior::getReadabilityRating)));

        if (ratings.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal sum = BigDecimal.ZERO;
        int count = 0;
        for (UserBehavior rating : ratings) {
            if (rating.getQualityRating() != null) {
                sum = sum.add(new BigDecimal(rating.getQualityRating()));
                count++;
            }
            if (rating.getReadabilityRating() != null) {
                sum = sum.add(new BigDecimal(rating.getReadabilityRating()));
                count++;
            }
        }

        if (count == 0) {
            return BigDecimal.ZERO;
        }

        return sum.divide(new BigDecimal(count), 2, java.math.RoundingMode.HALF_UP);
    }

    @Override
    public void recordSearch(Long userId, String searchQuery) {
        UserBehavior behavior = new UserBehavior();
        behavior.setUserId(userId);
        behavior.setDocumentId(0L);
        behavior.setBehaviorType("SEARCH");
        behavior.setBehaviorIntensity(BigDecimal.ONE);
        behavior.setSearchQuery(searchQuery);
        behavior.setCreateTime(LocalDateTime.now());
        this.save(behavior);
    }

    @Override
    public int countByDocumentAndType(Long documentId, String behaviorType) {
        return (int) this.count(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserBehavior>()
                        .eq(UserBehavior::getDocumentId, documentId)
                        .eq(UserBehavior::getBehaviorType, behaviorType));
    }

    @Override
    public void removeByUserAndDocumentAndType(Long userId, Long documentId, String behaviorType) {
        this.remove(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserBehavior>()
                        .eq(UserBehavior::getUserId, userId)
                        .eq(UserBehavior::getDocumentId, documentId)
                        .eq(UserBehavior::getBehaviorType, behaviorType));
    }

    @Override
    public java.util.List<Long> getDocumentIdsByUserAndType(Long userId, String behaviorType) {
        return this.list(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserBehavior>()
                        .eq(UserBehavior::getUserId, userId)
                        .eq(UserBehavior::getBehaviorType, behaviorType)
                        .select(UserBehavior::getDocumentId))
                .stream()
                .map(UserBehavior::getDocumentId)
                .distinct()
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public void batchRemoveByUserAndDocumentAndType(Long userId, java.util.List<Long> documentIds,
            String behaviorType) {
        this.remove(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserBehavior>()
                        .eq(UserBehavior::getUserId, userId)
                        .in(UserBehavior::getDocumentId, documentIds)
                        .eq(UserBehavior::getBehaviorType, behaviorType));
    }

    @Override
    public void removeAllByUserAndType(Long userId, String behaviorType) {
        this.remove(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserBehavior>()
                        .eq(UserBehavior::getUserId, userId)
                        .eq(UserBehavior::getBehaviorType, behaviorType));
    }

    @Override
    public boolean hasFavorited(Long userId, Long documentId) {
        if (userId == null || documentId == null) {
            return false;
        }
        return this.count(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserBehavior>()
                        .eq(UserBehavior::getUserId, userId)
                        .eq(UserBehavior::getDocumentId, documentId)
                        .eq(UserBehavior::getBehaviorType, "FAVORITE")) > 0;
    }

    @Override
    public java.util.Map<String, java.math.BigDecimal> calculateDocumentScores(Long documentId) {
        java.util.Map<String, java.math.BigDecimal> scores = new java.util.HashMap<>();

        if (documentId == null) {
            scores.put("qualityScore", BigDecimal.ZERO);
            scores.put("readabilityScore", BigDecimal.ZERO);
            return scores;
        }

        java.util.List<UserBehavior> ratings = this.list(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserBehavior>()
                        .eq(UserBehavior::getDocumentId, documentId)
                        .eq(UserBehavior::getBehaviorType, "RATE"));

        if (ratings.isEmpty()) {
            scores.put("qualityScore", BigDecimal.ZERO);
            scores.put("readabilityScore", BigDecimal.ZERO);
            return scores;
        }

        BigDecimal qualitySum = BigDecimal.ZERO;
        BigDecimal readabilitySum = BigDecimal.ZERO;
        int qualityCount = 0;
        int readabilityCount = 0;

        for (UserBehavior rating : ratings) {
            if (rating.getQualityRating() != null) {
                qualitySum = qualitySum.add(new BigDecimal(rating.getQualityRating()));
                qualityCount++;
            }
            if (rating.getReadabilityRating() != null) {
                readabilitySum = readabilitySum.add(new BigDecimal(rating.getReadabilityRating()));
                readabilityCount++;
            }
        }

        BigDecimal avgQualityScore = qualityCount > 0
                ? qualitySum.divide(new BigDecimal(qualityCount), 2, java.math.RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal avgReadabilityScore = readabilityCount > 0
                ? readabilitySum.divide(new BigDecimal(readabilityCount), 2, java.math.RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        scores.put("qualityScore", avgQualityScore);
        scores.put("readabilityScore", avgReadabilityScore);

        return scores;
    }
}
