package com.taoxier.smartdochub.comment.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评论表
 */
@Getter
@Setter
@TableName("doc_comment")
public class Comment implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 评论ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 文档ID
     */
    @TableField("document_id")
    private Long documentId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 评论内容
     */
    @TableField("content")
    private String content;

    /**
     * 父评论ID（用于回复）
     */
    @TableField("parent_id")
    private Long parentId;

    /**
     * AI审核状态（PASS, WARN, REJECT, PENDING）
     */
    @TableField("audit_status")
    private String auditStatus;

    /**
     * 审核结果置信度
     */
    @TableField("audit_confidence")
    private Double auditConfidence;

    /**
     * 审核时间
     */
    @TableField("audit_time")
    private LocalDateTime auditTime;

    /**
     * 审核原因
     */
    @TableField("audit_reason")
    private String auditReason;

    /**
     * 点赞数
     */
    @TableField("like_count")
    private Integer likeCount;

    /**
     * 状态（0-正常，1-删除）
     */
    @TableField("status")
    private Byte status;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;

    /**
     * 用户名（非数据库字段，用于返回前端）
     */
    @TableField(exist = false)
    private String userName;

    /**
     * 回复目标用户名（非数据库字段，用于返回前端）
     */
    @TableField(exist = false)
    private String replyToUserName;

    /**
     * 回复目标用户ID（非数据库字段，用于返回前端）
     */
    @TableField(exist = false)
    private Long replyToUserId;
}