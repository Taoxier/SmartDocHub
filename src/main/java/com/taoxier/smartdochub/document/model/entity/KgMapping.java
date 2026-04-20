package com.taoxier.smartdochub.document.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 文档-知识图谱映射表
 */
@Data
@TableName("doc_kg_mapping")
public class KgMapping {
    /**
     * 映射ID
     */
    @TableId
    private Long id;

    /**
     * 文档ID
     */
    @TableField("document_id")
    private Long documentId;

    /**
     * 图谱节点ID
     */
    @TableField("node_id")
    private Long nodeId;

    /**
     * 关联度
     */
    @TableField("relevance")
    private BigDecimal relevance;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;
}
