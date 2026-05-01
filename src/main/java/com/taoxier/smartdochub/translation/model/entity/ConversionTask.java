package com.taoxier.smartdochub.translation.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 格式转换任务表
 */
@Getter
@Setter
@TableName("doc_conversion_task")
public class ConversionTask implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务ID
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
     * 源格式
     */
    @TableField("source_format")
    private String sourceFormat;

    /**
     * 目标格式
     */
    @TableField("target_format")
    private String targetFormat;

    /**
     * 任务状态（PENDING, PROCESSING, COMPLETED, FAILED）
     */
    @TableField("status")
    private String status;

    /**
     * 进度（0-100）
     */
    @TableField("progress")
    private Integer progress;

    /**
     * 转换结果文件路径
     */
    @TableField("result_path")
    private String resultPath;

    /**
     * 错误信息
     */
    @TableField("error_message")
    private String errorMessage;

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
}