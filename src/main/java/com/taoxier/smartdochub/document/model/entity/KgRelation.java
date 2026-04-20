package com.taoxier.smartdochub.document.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 知识图谱关系表
 */
@Data
@TableName("kg_relation")
public class KgRelation {
    /**
     * 关系ID
     */
    @TableId
    private Long id;

    /**
     * 源节点ID
     */
    @TableField("source_node_id")
    private Long sourceNodeId;

    /**
     * 目标节点ID
     */
    @TableField("target_node_id")
    private Long targetNodeId;

    /**
     * 关系类型
     */
    @TableField("relation_type")
    private String relationType;

    /**
     * 关系权重
     */
    @TableField("weight")
    private BigDecimal weight;

    /**
     * 关系元数据
     */
    @TableField("metadata")
    private String metadata;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;
}
