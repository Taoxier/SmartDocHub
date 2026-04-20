package com.taoxier.smartdochub.system.model.event;

import lombok.Data;

/**
 * 字典更新事件
 */
@Data
public class DictEvent {
    /**
     * 字典编码
     */
    private String dictCode;
    
    /**
     * 时间戳
     */
    private long timestamp;

    public DictEvent(String dictCode) {
        this.dictCode = dictCode;
        this.timestamp = System.currentTimeMillis();
    }
} 
