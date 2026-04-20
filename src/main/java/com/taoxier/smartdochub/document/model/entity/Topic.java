package com.taoxier.smartdochub.document.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 文档主题表
 * </p>
 *
 * @author taoxier
 * @since 2025-10-16
 */
@Getter
@Setter
@TableName("doc_topic")
public class Topic implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主题ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 文档ID
     */
    @TableField("document_id")
    private Long documentId;

    /**
     * 版本号
     */
    @TableField("version_number")
    private Integer versionNumber;

    /**
     * 主题类型: KEYWORD, ENTITY, CONCEPT
     */
    @TableField("topic_type")
    private String topicType;

    /**
     * 主题值
     */
    @TableField("topic_value")
    private String topicValue;

    /**
     * 置信度
     */
    @TableField("confidence")
    private BigDecimal confidence;

    /**
     * 权重
     */
    @TableField("weight")
    private BigDecimal weight;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;
}
