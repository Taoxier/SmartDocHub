package com.taoxier.smartdochub.document.util.parse;

import com.taoxier.smartdochub.document.model.entity.ContentChunk;
import lombok.Data;

import java.util.List;

/**
 * @Author taoxier
 * @Date 2025/10/17 下午5:05
 * @描述 文档解析结果模型（仅存储数据，不包含业务逻辑）
 */
@Data
public class DocumentParseResult {
    /** 解析后的全文文本 */
    private String fullText;
    /** 总页数 */
    private int pageCount;
    /** 总字数 */
    private int wordCount;
    /** 总字符数 */
    private int characterCount;
    /** 内容分块列表（按类型区分） */
    private List<ContentChunk> chunks;
}