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

@Getter
@Setter
@TableName("user_behavior")
public class UserBehavior implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("document_id")
    private Long documentId;

    @TableField("behavior_type")
    private String behaviorType;

    @TableField("behavior_intensity")
    private BigDecimal behaviorIntensity;

    @TableField("duration_seconds")
    private Integer durationSeconds;

    @TableField("search_query")
    private String searchQuery;

    @TableField("rating_value")
    private Byte ratingValue;

    @TableField("create_time")
    private LocalDateTime createTime;
}
