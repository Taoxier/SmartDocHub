package com.taoxier.smartdochub.document.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@TableName("doc_document_audit")
public class DocumentAudit implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("document_id")
    private Long documentId;

    @TableField("audit_type")
    private String auditType;

    @TableField("text_status")
    private String textStatus;

    @TableField("image_status")
    private String imageStatus;

    @TableField("overall_status")
    private String overallStatus;

    @TableField("text_details")
    private String textDetails;

    @TableField("image_details")
    private String imageDetails;

    @TableField("audit_time")
    private LocalDateTime auditTime;

    @TableField("operator")
    private String operator;

    @TableField("status")
    private String status;
}
