package com.taoxier.smartdochub.comment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taoxier.smartdochub.comment.model.entity.Comment;

/**
 * 评论Mapper
 */
public interface CommentMapper extends BaseMapper<Comment> {

    /**
     * 根据文档ID获取评论列表
     * @param documentId 文档ID
     * @return 评论列表
     */
    java.util.List<Comment> selectByDocumentId(Long documentId);
}