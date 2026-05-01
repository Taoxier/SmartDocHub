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
 * 评论审核记录表
 */
@Getter
@Setter
@TableName("doc_comment_audit")
public class CommentAudit implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 审核ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 评论ID
     */
    @TableField("comment_id")
    private Long commentId;

    /**
     * 审核结果（PASS, WARN, REJECT）
     */
    @TableField("result")
    private String result;

    /**
     * 置信度
     */
    @TableField("confidence")
    private Double confidence;

    /**
     * 审核模型
     */
    @TableField("model")
    private String model;

    /**
     * 审核原因
     */
    @TableField("reason")
    private String reason;

    /**
     * 审核时间
     */
    @TableField("audit_time")
    private LocalDateTime auditTime;

    /**
     * 审核人（AI或人工）
     */
    @TableField("auditor")
    private String auditor;
}