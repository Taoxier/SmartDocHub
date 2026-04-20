package com.taoxier.smartdochub.document.service;

import com.taoxier.smartdochub.document.model.entity.KgNode;
import com.taoxier.smartdochub.document.model.entity.KgRelation;

import java.util.List;

/**
 * 知识图谱服务
 */
public interface KgService {
    /**
     * 构建文档的知识图谱
     */
    void buildDocumentKnowledgeGraph(Long documentId);

    /**
     * 批量构建知识图谱
     */
    void batchBuildKnowledgeGraph(List<Long> documentIds);

    /**
     * 获取文档的知识图谱节点
     */
    List<KgNode> getDocumentNodes(Long documentId);

    /**
     * 获取文档的知识图谱关系
     */
    List<KgRelation> getDocumentRelations(Long documentId);

    /**
     * 清理文档的知识图谱
     */
    void clearDocumentKnowledgeGraph(Long documentId);

    /**
     * 获取文档的知识图谱数据
     */
    java.util.Map<String, Object> getDocumentKnowledgeGraph(Long documentId);

    /**
     * 获取知识图谱预览数据（全局高频节点和关系）
     * 
     * @param nodeLimit 节点数量限制
     * @param relationLimit 关系数量限制
     * @return 知识图谱预览数据
     */
    java.util.Map<String, Object> getKnowledgeGraphPreview(int nodeLimit, int relationLimit);
}