package com.taoxier.smartdochub.comment.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 敏感词实体类
 */
@Data
@TableName("doc_sensitive_word")
public class SensitiveWord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 敏感词
     */
    private String word;

    /**
     * 分类: PORN, VIOLENCE, POLITICS, AD, ILLEGAL
     */
    private String category;

    /**
     * 级别: 1-警告, 2-拒绝
     */
    private Integer level;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
