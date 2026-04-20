package com.taoxier.smartdochub.document.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 异步任务表
 * </p>
 *
 * @author taoxier
 * @since 2026-04-11
 */
@Getter
@Setter
@TableName("doc_async_task")
public class AsyncTask implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 任务类型: DOCUMENT_PARSE, SIMILARITY_CHECK, AI_DETECTION, KEYWORD_EXTRACTION
     */
    @TableField("task_type")
    private String taskType;

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
     * 任务状态: PENDING, RUNNING, COMPLETED, FAILED
     */
    @TableField("status")
    private String status;

    /**
     * 任务进度(0-100)
     */
    @TableField("progress")
    private Integer progress;

    /**
     * 任务结果
     */
    @TableField("result")
    private JsonNode result;

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
     * 开始时间
     */
    @TableField("start_time")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @TableField("end_time")
    private LocalDateTime endTime;
}
