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
    public void recordRating(Long userId, Long documentId, Byte ratingValue) {
        UserBehavior behavior = new UserBehavior();
        behavior.setUserId(userId);
        behavior.setDocumentId(documentId);
        behavior.setBehaviorType("RATE");
        behavior.setBehaviorIntensity(BigDecimal.ONE);
        behavior.setRatingValue(ratingValue);
        behavior.setCreateTime(LocalDateTime.now());
        this.save(behavior);
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
    public void batchRemoveByUserAndDocumentAndType(Long userId, java.util.List<Long> documentIds, String behaviorType) {
        this.remove(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserBehavior>()
                        .eq(UserBehavior::getUserId, userId)
                        .in(UserBehavior::getDocumentId, documentIds)
                        .eq(UserBehavior::getBehaviorType, behaviorType)
        );
    }

    @Override
    public void removeAllByUserAndType(Long userId, String behaviorType) {
        this.remove(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserBehavior>()
                        .eq(UserBehavior::getUserId, userId)
                        .eq(UserBehavior::getBehaviorType, behaviorType)
        );
    }
}
