package com.taoxier.smartdochub.ai.service;

import com.taoxier.smartdochub.comment.model.vo.CommentAuditResultVO;

import java.util.List;
import java.util.Map;

/**
 * AI服务接口
 */
public interface AIService {

    /**
     * 智能分类文档
     * 
     * @param content 文档内容
     * @return 分类结果
     */
    String classifyDocument(String content);

    /**
     * 生成文档标签
     * 
     * @param content 文档内容
     * @return 标签列表
     */
    List<String> generateTags(String content);

    /**
     * 生成文档描述
     * 
     * @param content 文档内容
     * @return 文档描述
     */
    String generateDescription(String content);

    /**
     * 审核评论
     * 
     * @param content 评论内容
     * @return 审核结果
     */
    String auditComment(String content);

    /**
     * 审核评论（详细结果 - 支持敏感词+AI情感双审核）
     * 
     * @param content 评论内容
     * @return 详细审核结果，包含敏感词检测和AI情感分析
     */
    CommentAuditResultVO auditCommentDetail(String content);

    /**
     * 审核文档
     * 
     * @param documentId  文档ID
     * @param auditType   审核类型
     * @param storagePath 文档存储路径
     * @return 审核结果
     */
    String auditDocument(Long documentId, String auditType, String storagePath);

    /**
     * 翻译文档
     * 
     * @param documentId     文档ID
     * @param sourceLanguage 源语言
     * @param targetLanguage 目标语言
     * @return 翻译结果文件路径
     */
    String translateDocument(Long documentId, String sourceLanguage, String targetLanguage);

    /**
     * 转换文档格式
     * 
     * @param documentId   文档ID
     * @param sourceFormat 源格式
     * @param targetFormat 目标格式
     * @return 转换结果文件路径
     */
    String convertDocument(Long documentId, String sourceFormat, String targetFormat);
}
