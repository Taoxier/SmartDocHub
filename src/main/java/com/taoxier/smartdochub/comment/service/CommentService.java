package com.taoxier.smartdochub.comment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.taoxier.smartdochub.comment.model.entity.Comment;

import java.util.List;

/**
 * 评论服务
 */
public interface CommentService extends IService<Comment> {

    /**
     * 添加评论
     * @param comment 评论对象
     * @return 评论ID
     */
    Long addComment(Comment comment);

    /**
     * 根据文档ID获取评论列表
     * @param documentId 文档ID
     * @return 评论列表
     */
    List<Comment> getCommentsByDocumentId(Long documentId);

    /**
     * 审核评论
     * @param commentId 评论ID
     * @param result 审核结果
     * @param reason 审核原因
     */
    void auditComment(Long commentId, String result, String reason);
}