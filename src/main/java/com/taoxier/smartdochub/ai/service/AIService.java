package com.taoxier.smartdochub.ai.service;

import java.util.List;
import java.util.Map;

/**
 * AI服务接口
 */
public interface AIService {

    /**
     * 智能分类文档
     * @param content 文档内容
     * @return 分类结果
     */
    String classifyDocument(String content);

    /**
     * 生成文档标签
     * @param content 文档内容
     * @return 标签列表
     */
    List<String> generateTags(String content);

    /**
     * 生成文档描述
     * @param content 文档内容
     * @return 文档描述
     */
    String generateDescription(String content);
}
