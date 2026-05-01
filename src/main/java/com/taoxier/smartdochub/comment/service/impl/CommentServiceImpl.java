package com.taoxier.smartdochub.comment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.taoxier.smartdochub.comment.mapper.CommentMapper;
import com.taoxier.smartdochub.comment.model.entity.Comment;
import com.taoxier.smartdochub.comment.service.CommentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taoxier.smartdochub.ai.service.AIService;
import com.taoxier.smartdochub.system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 评论服务实现
 */
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    @Autowired
    private AIService aiService;

    @Autowired
    private UserService userService;

    @Override
    public Long addComment(Comment comment) {
        // 设置默认值
        comment.setAuditStatus("PENDING");
        comment.setStatus((byte) 0);
        comment.setCreateTime(LocalDateTime.now());
        comment.setUpdateTime(LocalDateTime.now());

        // 保存评论
        save(comment);

        // 异步进行AI审核
        new Thread(() -> {
            try {
                String result = aiService.auditComment(comment.getContent());
                // 更新审核状态
                comment.setAuditStatus(result);
                comment.setAuditTime(LocalDateTime.now());
                updateById(comment);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        return comment.getId();
    }

    @Override
    public List<Comment> getCommentsByDocumentId(Long documentId) {
        List<Comment> comments = list(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getDocumentId, documentId)
                .eq(Comment::getStatus, 0)
                .eq(Comment::getAuditStatus, "APPROVED")
                .orderByAsc(Comment::getCreateTime));

        // 收集所有用户ID（包括评论作者和被回复的用户）
        Set<Long> userIds = new java.util.HashSet<>();
        for (Comment comment : comments) {
            userIds.add(comment.getUserId());
            if (comment.getParentId() != null) {
                // 找出被回复评论的用户ID
                for (Comment parent : comments) {
                    if (parent.getId().equals(comment.getParentId())) {
                        userIds.add(parent.getUserId());
                        break;
                    }
                }
            }
        }

        // 批量获取用户信息
        Map<Long, String> userNameMap = new java.util.HashMap<>();
        if (!userIds.isEmpty()) {
            List<com.taoxier.smartdochub.system.model.entity.User> users = userService.list(
                    new LambdaQueryWrapper<com.taoxier.smartdochub.system.model.entity.User>()
                            .in(com.taoxier.smartdochub.system.model.entity.User::getId, userIds));
            for (com.taoxier.smartdochub.system.model.entity.User user : users) {
                userNameMap.put(user.getId(), user.getUsername());
            }
        }

        // 设置用户名和回复目标用户名
        for (Comment comment : comments) {
            comment.setUserName(userNameMap.get(comment.getUserId()));

            if (comment.getParentId() != null) {
                // 找出被回复的评论
                for (Comment parent : comments) {
                    if (parent.getId().equals(comment.getParentId())) {
                        comment.setReplyToUserId(parent.getUserId());
                        comment.setReplyToUserName(userNameMap.get(parent.getUserId()));
                        break;
                    }
                }
            }
        }

        return comments;
    }

    @Override
    public void auditComment(Long commentId, String result, String reason) {
        Comment comment = getById(commentId);
        if (comment != null) {
            comment.setAuditStatus(result);
            comment.setAuditReason(reason);
            comment.setAuditTime(LocalDateTime.now());
            updateById(comment);
        }
    }
}