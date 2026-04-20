package com.taoxier.smartdochub.document.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.taoxier.smartdochub.document.mapper.KgMappingMapper;
import com.taoxier.smartdochub.document.mapper.KgNodeMapper;
import com.taoxier.smartdochub.document.mapper.KgRelationMapper;
import com.taoxier.smartdochub.document.model.entity.ContentChunk;
import com.taoxier.smartdochub.document.model.entity.KgMapping;
import com.taoxier.smartdochub.document.model.entity.KgNode;
import com.taoxier.smartdochub.document.model.entity.KgRelation;
import com.taoxier.smartdochub.document.model.entity.Topic;
import com.taoxier.smartdochub.document.service.ContentChunkService;
import com.taoxier.smartdochub.document.service.KgService;
import com.taoxier.smartdochub.document.service.TopicService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 知识图谱服务实现
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class KgServiceImpl implements KgService {

    private final KgNodeMapper kgNodeMapper;
    private final KgRelationMapper kgRelationMapper;
    private final KgMappingMapper kgMappingMapper;
    private final ContentChunkService contentChunkService;
    private final TopicService topicService;

    @Override
    @Transactional
    public void buildDocumentKnowledgeGraph(Long documentId) {
        log.info("开始构建文档知识图谱，文档ID: {}", documentId);

        try {
            // 1. 获取文档内容
            String documentContent = getDocumentContent(documentId);
            if (documentContent == null) {
                log.warn("文档内容为空，无法构建知识图谱，文档ID: {}", documentId);
                return;
            }

            // 2. 清理旧的知识图谱数据
            clearDocumentKnowledgeGraph(documentId);

            // 3. 实体识别
            List<KgNode> nodes = extractEntities(documentContent, documentId);

            // 4. 保存节点
            for (KgNode node : nodes) {
                // 检查节点是否已存在
                KgNode existingNode = kgNodeMapper.selectOne(
                        new LambdaQueryWrapper<KgNode>()
                                .eq(KgNode::getNodeType, node.getNodeType())
                                .eq(KgNode::getNodeName, node.getNodeName()));
                if (existingNode == null) {
                    kgNodeMapper.insert(node);
                } else {
                    node.setId(existingNode.getId());
                }
            }

            // 5. 关系抽取（节点保存后，ID已设置）
            List<KgRelation> relations = extractRelations(nodes, documentContent);

            // 6. 保存关系
            for (KgRelation relation : relations) {
                // 检查关系是否已存在
                KgRelation existingRelation = kgRelationMapper.selectOne(
                        new LambdaQueryWrapper<KgRelation>()
                                .eq(KgRelation::getSourceNodeId, relation.getSourceNodeId())
                                .eq(KgRelation::getTargetNodeId, relation.getTargetNodeId())
                                .eq(KgRelation::getRelationType, relation.getRelationType()));
                if (existingRelation == null) {
                    kgRelationMapper.insert(relation);
                }
            }

            // 6. 建立文档与节点的映射
            for (KgNode node : nodes) {
                KgMapping mapping = new KgMapping();
                mapping.setDocumentId(documentId);
                mapping.setNodeId(node.getId());
                mapping.setRelevance(new BigDecimal("1.0"));
                mapping.setCreateTime(LocalDateTime.now());
                kgMappingMapper.insert(mapping);
            }

            log.info("文档知识图谱构建完成，文档ID: {}", documentId);
        } catch (Exception e) {
            log.error("构建文档知识图谱失败，文档ID: {}", documentId, e);
        }
    }

    /**
     * 从ContentChunk中获取文档内容
     */
    private String getDocumentContent(Long documentId) {
        try {
            // 从ContentChunk中获取文档内容
            List<ContentChunk> chunks = contentChunkService.list(
                    new LambdaQueryWrapper<ContentChunk>()
                            .eq(ContentChunk::getDocumentId, documentId)
                            .orderByAsc(ContentChunk::getChunkIndex));

            if (chunks.isEmpty()) {
                log.warn("文档没有内容分块，文档ID: {}", documentId);
                // 返回空字符串，让extractEntities从Topic中提取实体
                return "";
            }

            // 拼接所有内容分块
            StringBuilder contentBuilder = new StringBuilder();
            for (ContentChunk chunk : chunks) {
                if (chunk.getContentText() != null) {
                    contentBuilder.append(chunk.getContentText()).append(" ");
                }
            }

            return contentBuilder.toString().trim();
        } catch (Exception e) {
            log.error("获取文档内容失败，文档ID: {}", documentId, e);
            // 失败时返回空字符串，让extractEntities从Topic中提取实体
            return "";
        }
    }

    @Override
    public void batchBuildKnowledgeGraph(List<Long> documentIds) {
        for (Long documentId : documentIds) {
            buildDocumentKnowledgeGraph(documentId);
        }
    }

    @Override
    public List<KgNode> getDocumentNodes(Long documentId) {
        List<KgMapping> mappings = kgMappingMapper.selectList(
                new LambdaQueryWrapper<KgMapping>()
                        .eq(KgMapping::getDocumentId, documentId));

        List<Long> nodeIds = mappings.stream()
                .map(KgMapping::getNodeId)
                .collect(Collectors.toList());

        if (nodeIds.isEmpty()) {
            return Collections.emptyList();
        }

        return kgNodeMapper.selectBatchIds(nodeIds);
    }

    @Override
    public List<KgRelation> getDocumentRelations(Long documentId) {
        List<KgNode> nodes = getDocumentNodes(documentId);
        if (nodes.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> nodeIds = nodes.stream()
                .map(KgNode::getId)
                .collect(Collectors.toList());

        return kgRelationMapper.selectList(
                new LambdaQueryWrapper<KgRelation>()
                        .in(KgRelation::getSourceNodeId, nodeIds)
                        .in(KgRelation::getTargetNodeId, nodeIds));
    }

    @Override
    @Transactional
    public void clearDocumentKnowledgeGraph(Long documentId) {
        // 删除文档与节点的映射
        kgMappingMapper.delete(
                new LambdaQueryWrapper<KgMapping>()
                        .eq(KgMapping::getDocumentId, documentId));

        // 注意：这里不删除节点和关系，因为它们可能被其他文档引用
    }

    /**
     * 提取实体
     */
    private List<KgNode> extractEntities(String content, Long documentId) {
        List<KgNode> nodes = new ArrayList<>();

        try {
            // 首先从文档的Topic中提取实体
            log.info("开始从文档ID: {}的Topic中提取实体", documentId);
            List<Topic> topics = topicService.list(
                    new LambdaQueryWrapper<Topic>()
                            .eq(Topic::getDocumentId, documentId)
                            .orderByDesc(Topic::getCreateTime));
            log.info("获取到的Topic数量: {}", topics.size());

            log.info("文档ID: {}的Topic数量: {}", documentId, topics.size());
            for (Topic topic : topics) {
                log.info("Topic: type={}, value={}", topic.getTopicType(), topic.getTopicValue());
            }

            if (!topics.isEmpty()) {
                log.info("从Topic中提取实体");
                // 从Topic中提取实体
                for (Topic topic : topics) {
                    String topicValue = topic.getTopicValue();
                    String topicType = topic.getTopicType();

                    // 根据Topic类型映射到KgNode类型
                    String nodeType;
                    switch (topicType) {
                        case "ENTITY":
                            nodeType = "ENTITY";
                            break;
                        case "CONCEPT":
                            nodeType = "CONCEPT";
                            break;
                        case "TECHNOLOGY":
                            nodeType = "TECHNOLOGY";
                            break;
                        case "KEYWORD":
                            // 对于KEYWORD类型，根据内容判断是技术还是概念
                            nodeType = isTechnology(topicValue) ? "TECHNOLOGY" : "CONCEPT";
                            break;
                        default:
                            nodeType = "CONCEPT";
                    }

                    log.info("添加实体: type={}, name={}", nodeType, topicValue);
                    addNodeIfNotExists(nodes, nodeType, topicValue);
                }
            } else {
                log.info("文档没有Topic，使用规则从内容中提取实体");
                // 如果没有Topic，使用规则从内容中提取实体
                // 提取概念实体
                List<String> concepts = Arrays.asList("人工智能", "机器学习", "深度学习", "自然语言处理", "计算机视觉", "知识图谱");
                for (String concept : concepts) {
                    if (content.contains(concept)) {
                        addNodeIfNotExists(nodes, "CONCEPT", concept);
                    }
                }

                // 提取技术名词
                List<String> technologies = Arrays.asList("AI", "ML", "DL", "NLP", "CV");
                for (String tech : technologies) {
                    if (content.contains(tech)) {
                        addNodeIfNotExists(nodes, "TECHNOLOGY", tech);
                    }
                }
            }

            // 如果没有提取到任何实体，使用默认实体
            if (nodes.isEmpty()) {
                addNodeIfNotExists(nodes, "CONCEPT", "文档主题");
                addNodeIfNotExists(nodes, "CONCEPT", "内容分析");
            }

        } catch (Exception e) {
            log.error("实体识别失败", e);
            // 失败时使用默认实体
            addNodeIfNotExists(nodes, "CONCEPT", "文档主题");
            addNodeIfNotExists(nodes, "CONCEPT", "内容分析");
        }

        return nodes;
    }

    /**
     * 提取关系
     */
    private List<KgRelation> extractRelations(List<KgNode> nodes, String content) {
        // TODO: 基于共现统计和规则提取关系
        List<KgRelation> relations = new ArrayList<>();

        // 模拟提取一些关系
        if (nodes.size() >= 2) {
            for (int i = 0; i < nodes.size() - 1; i++) {
                KgNode sourceNode = nodes.get(i);
                KgNode targetNode = nodes.get(i + 1);

                KgRelation relation = new KgRelation();
                relation.setSourceNodeId(sourceNode.getId());
                relation.setTargetNodeId(targetNode.getId());
                relation.setRelationType("RELATED_TO");
                relation.setWeight(new BigDecimal("1.0"));
                relation.setCreateTime(LocalDateTime.now());
                relations.add(relation);
            }
        }

        return relations;
    }

    /**
     * 如果节点不存在则添加
     */
    private void addNodeIfNotExists(List<KgNode> nodes, String type, String name) {
        boolean exists = nodes.stream()
                .anyMatch(node -> node.getNodeType().equals(type) && node.getNodeName().equals(name));
        if (!exists) {
            KgNode node = new KgNode();
            node.setNodeType(type);
            node.setNodeName(name);
            node.setNodeDescription(name + "相关概念");
            node.setWeight(new BigDecimal("1.0"));
            node.setCreateTime(LocalDateTime.now());
            nodes.add(node);
        }
    }

    @Override
    public java.util.Map<String, Object> getDocumentKnowledgeGraph(Long documentId) {
        java.util.Map<String, Object> result = new java.util.HashMap<>();

        // 1. 获取文档关联的所有节点
        List<KgMapping> mappings = kgMappingMapper.selectList(
                new LambdaQueryWrapper<KgMapping>()
                        .eq(KgMapping::getDocumentId, documentId));

        if (mappings.isEmpty()) {
            // 如果没有节点，返回空数据
            result.put("nodes", new ArrayList<>());
            result.put("links", new ArrayList<>());
            return result;
        }

        // 2. 获取节点ID列表
        List<Long> nodeIds = mappings.stream()
                .map(KgMapping::getNodeId)
                .collect(Collectors.toList());

        // 3. 获取节点信息
        List<KgNode> nodes = kgNodeMapper.selectBatchIds(nodeIds);

        // 4. 获取这些节点之间的关系
        List<KgRelation> relations = kgRelationMapper.selectList(
                new LambdaQueryWrapper<KgRelation>()
                        .in(KgRelation::getSourceNodeId, nodeIds)
                        .in(KgRelation::getTargetNodeId, nodeIds));

        // 5. 转换为前端需要的格式
        List<java.util.Map<String, Object>> frontendNodes = new ArrayList<>();
        for (KgNode node : nodes) {
            java.util.Map<String, Object> frontendNode = new java.util.HashMap<>();
            frontendNode.put("id", node.getId());
            frontendNode.put("label", node.getNodeName());
            frontendNode.put("type", node.getNodeType());
            frontendNodes.add(frontendNode);
        }

        List<java.util.Map<String, Object>> frontendLinks = new ArrayList<>();
        for (KgRelation relation : relations) {
            java.util.Map<String, Object> frontendLink = new java.util.HashMap<>();
            frontendLink.put("source", relation.getSourceNodeId());
            frontendLink.put("target", relation.getTargetNodeId());
            frontendLink.put("label", relation.getRelationType());
            frontendLinks.add(frontendLink);
        }

        result.put("nodes", frontendNodes);
        result.put("links", frontendLinks);

        return result;
    }

    @Override
    public Map<String, Object> getKnowledgeGraphPreview(int nodeLimit, int relationLimit) {
        Map<String, Object> result = new HashMap<>();

        // 获取高频节点
        List<KgNode> topNodes = kgNodeMapper.selectTopNodes(nodeLimit);

        // 获取节点ID列表
        List<Long> nodeIds = topNodes.stream().map(KgNode::getId).collect(Collectors.toList());

        // 转换为前端格式
        List<Map<String, Object>> frontendNodes = new ArrayList<>();
        for (KgNode node : topNodes) {
            Map<String, Object> frontendNode = new HashMap<>();
            frontendNode.put("id", node.getId());
            frontendNode.put("name", node.getNodeName());
            frontendNode.put("category", node.getNodeType());
            frontendNodes.add(frontendNode);
        }

        // 获取与这些节点相关的关系
        List<Map<String, Object>> frontendLinks = new ArrayList<>();
        if (!nodeIds.isEmpty()) {
            LambdaQueryWrapper<KgRelation> relationWrapper = new LambdaQueryWrapper<>();
            relationWrapper.in(KgRelation::getSourceNodeId, nodeIds)
                    .or()
                    .in(KgRelation::getTargetNodeId, nodeIds)
                    .last("LIMIT " + relationLimit);
            List<KgRelation> relations = kgRelationMapper.selectList(relationWrapper);

            for (KgRelation relation : relations) {
                Map<String, Object> frontendLink = new HashMap<>();
                frontendLink.put("source", relation.getSourceNodeId());
                frontendLink.put("target", relation.getTargetNodeId());
                frontendLink.put("label", relation.getRelationType());
                frontendLinks.add(frontendLink);
            }
        }

        result.put("nodes", frontendNodes);
        result.put("links", frontendLinks);

        return result;
    }

    /**
     * 判断关键词是否为技术术语
     */
    private boolean isTechnology(String keyword) {
        // 技术术语列表
        List<String> techTerms = Arrays.asList(
                "AI", "ML", "DL", "NLP", "CV", "API", "SDK", "UI", "UX", "HTML", "CSS", "JavaScript",
                "Python", "Java", "C++", "SQL", "NoSQL", "MongoDB", "MySQL", "PostgreSQL", "Redis",
                "Docker", "Kubernetes", "Git", "GitHub", "GitLab", "AWS", "Azure", "GCP", "Linux",
                "Windows", "MacOS", "React", "Vue", "Angular", "Node.js", "Spring", "Flask", "Django",
                "TensorFlow", "PyTorch", "Keras", "Scikit-learn", "Pandas", "NumPy", "Matplotlib");

        // 技术相关词汇
        List<String> techRelatedWords = Arrays.asList(
                "技术", "算法", "系统", "软件", "硬件", "网络", "数据库", "服务器", "客户端",
                "前端", "后端", "全栈", "开发", "编程", "编码", "测试", "部署", "架构",
                "框架", "库", "工具", "平台", "协议", "标准", "接口", "模块", "组件");

        // 检查是否在技术术语列表中
        if (techTerms.contains(keyword)) {
            return true;
        }

        // 检查是否包含技术相关词汇
        for (String techWord : techRelatedWords) {
            if (keyword.contains(techWord)) {
                return true;
            }
        }

        return false;
    }
}