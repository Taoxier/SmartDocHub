package com.taoxier.smartdochub.document.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.taoxier.smartdochub.common.exception.BusinessException;
import com.taoxier.smartdochub.document.mapper.ContentChunkMapper;
import com.taoxier.smartdochub.document.mapper.DocumentMapper;
import com.taoxier.smartdochub.document.mapper.SimilarityMapper;
import com.taoxier.smartdochub.document.model.dto.SimilarDocumentDTO;
import com.taoxier.smartdochub.document.model.dto.SimilarityResultDTO;
import com.taoxier.smartdochub.document.model.entity.ContentChunk;
import com.taoxier.smartdochub.document.model.entity.Document;
import com.taoxier.smartdochub.document.model.entity.Similarity;
import com.taoxier.smartdochub.document.service.SimilarityDetectionService;
import com.taoxier.smartdochub.document.service.DocumentVectorService;
import com.taoxier.smartdochub.document.service.SentenceVectorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Author taoxier
 * @Date 2025/10/20 下午5:35
 * @描述
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SimilarityDetectionServiceImpl implements SimilarityDetectionService {

    private final DocumentMapper documentMapper;
    private final SimilarityMapper similarityMapper;
    private final ContentChunkMapper contentChunkMapper;
    private final DocumentVectorService documentVectorService;
    private final SentenceVectorService sentenceVectorService;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final int HASH_BITS = 64;
    private static final BigDecimal SIMILARITY_THRESHOLD = new BigDecimal("0.6");
    private static final int BATCH_SIZE = 200;
    private static final int CACHE_EXPIRE_TIME = 7 * 24 * 60 * 60;

    @Override
    @Async("taskExecutor")
    public SimilarityResultDTO checkSimilarity(Long documentId, String content) {
        try {
            log.info("开始重复率检测，文档ID: {}", documentId);

            // 1. 尝试使用AI向量计算相似度
            SimilarityResultDTO result = checkSimilarityWithVector(documentId, content);
            if (result != null) {
                log.info("使用AI向量完成重复率检测，文档ID: {}, 最高相似度: {}", documentId, result.getOverallSimilarity());
                return result;
            }

            // 2. 如果向量计算失败，使用SimHash作为备用方案
            log.info("AI向量计算失败，使用SimHash作为备用方案，文档ID: {}", documentId);
            return checkSimilarityWithSimHash(documentId, content);

        } catch (Exception e) {
            log.error("重复率检测失败，文档ID: {}", documentId, e);
            throw new BusinessException("重复率检测失败: " + e.getMessage());
        }
    }

    /**
     * 使用AI向量计算相似度
     */
    private SimilarityResultDTO checkSimilarityWithVector(Long documentId, String content) {
        try {
            log.info("开始使用AI向量计算相似度，文档ID: {}", documentId);

            // 1. 生成当前文档的向量
            List<Double> currentVector = sentenceVectorService.generateVector(content);
            if (currentVector == null || currentVector.isEmpty()) {
                log.warn("生成当前文档向量失败，文档ID: {}", documentId);
                return null;
            }
            log.info("当前文档向量生成成功，维度: {}", currentVector.size());

            // 2. 保存当前文档的向量
            documentVectorService.saveOrUpdateVector(documentId, com.alibaba.fastjson.JSON.toJSONString(currentVector));
            log.info("当前文档向量保存成功，文档ID: {}", documentId);

            // 3. 分批次获取已有文档的向量进行比对（排除自己）
            List<Document> existingDocs = getExistingDocuments(documentId);
            log.info("获取到已有文档数量: {}, 文档ID: {}", existingDocs.size(), documentId);

            if (existingDocs.isEmpty()) {
                log.info("没有其他文档可对比，文档ID: {}", documentId);
                updateDocumentSimilarity(documentId, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                        new ArrayList<>());

                SimilarityResultDTO result = new SimilarityResultDTO();
                result.setDocumentId(documentId);
                result.setOverallSimilarity(BigDecimal.ZERO);
                result.setTextSimilarity(BigDecimal.ZERO);
                result.setTableSimilarity(BigDecimal.ZERO);
                result.setFormulaSimilarity(BigDecimal.ZERO);
                result.setSimilarDocuments(new ArrayList<>());
                result.setDetectionTime(LocalDateTime.now());

                return result;
            }

            // 4. 计算文档级相似度（AI向量）
            List<SimilarDocumentDTO> similarDocs = new ArrayList<>();
            BigDecimal maxSimilarity = BigDecimal.ZERO;
            BigDecimal textSimilarity = BigDecimal.ZERO;

            List<List<Document>> batches = splitIntoBatches(existingDocs, BATCH_SIZE);
            log.info("文档分 {} 个批次处理，每批最多 {} 个", batches.size(), BATCH_SIZE);

            for (int batchIdx = 0; batchIdx < batches.size(); batchIdx++) {
                List<Document> batch = batches.get(batchIdx);
                log.info("开始处理第 {} 批文档，文档数量: {}", batchIdx + 1, batch.size());

                List<SimilarDocumentDTO> batchSimilarDocs = processBatchWithVector(documentId, currentVector, batch);
                log.info("第 {} 批文档处理完成，找到 {} 个相似文档", batchIdx + 1, batchSimilarDocs.size());

                similarDocs.addAll(batchSimilarDocs);

                for (SimilarDocumentDTO similarDoc : batchSimilarDocs) {
                    if (similarDoc.getSimilarityScore().compareTo(maxSimilarity) > 0) {
                        maxSimilarity = similarDoc.getSimilarityScore();
                        textSimilarity = similarDoc.getSimilarityScore();
                    }
                }
            }
            log.info("文档级相似度计算完成，最高相似度: {}, 相似文档数量: {}", maxSimilarity, similarDocs.size());

            // 5. 用 SimHash 计算表格和公式相似度（文档级，不调API，速度快）
            BigDecimal tableSimilarity = BigDecimal.ZERO;
            BigDecimal formulaSimilarity = BigDecimal.ZERO;

            try {
                log.info("开始用SimHash计算表格/公式相似度，文档ID: {}", documentId);

                List<ContentChunk> currentChunks = contentChunkMapper.selectList(
                        new LambdaQueryWrapper<ContentChunk>()
                                .eq(ContentChunk::getDocumentId, documentId));

                StringBuilder tableText = new StringBuilder();
                StringBuilder formulaText = new StringBuilder();
                for (ContentChunk chunk : currentChunks) {
                    String chunkText = chunk.getContentText();
                    if (chunkText == null || chunkText.trim().isEmpty())
                        continue;
                    if ("TABLE".equals(chunk.getContentType())) {
                        tableText.append(chunkText).append(" ");
                    } else if ("FORMULA".equals(chunk.getContentType())) {
                        formulaText.append(chunkText).append(" ");
                    }
                }

                if (tableText.length() > 0) {
                    long tableHash = computeSimHash(tableText.toString());
                    for (Document existingDoc : existingDocs) {
                        List<ContentChunk> existingChunks = contentChunkMapper.selectList(
                                new LambdaQueryWrapper<ContentChunk>()
                                        .eq(ContentChunk::getDocumentId, existingDoc.getId())
                                        .eq(ContentChunk::getContentType, "TABLE")
                                        .isNotNull(ContentChunk::getContentText));
                        StringBuilder existingTableText = new StringBuilder();
                        for (ContentChunk ec : existingChunks) {
                            existingTableText.append(ec.getContentText()).append(" ");
                        }
                        if (existingTableText.length() > 0) {
                            BigDecimal sim = calculateSimilarity(tableHash,
                                    computeSimHash(existingTableText.toString()));
                            if (sim.compareTo(tableSimilarity) > 0) {
                                tableSimilarity = sim;
                            }
                        }
                    }
                }

                if (formulaText.length() > 0) {
                    long formulaHash = computeSimHash(formulaText.toString());
                    for (Document existingDoc : existingDocs) {
                        List<ContentChunk> existingChunks = contentChunkMapper.selectList(
                                new LambdaQueryWrapper<ContentChunk>()
                                        .eq(ContentChunk::getDocumentId, existingDoc.getId())
                                        .eq(ContentChunk::getContentType, "FORMULA")
                                        .isNotNull(ContentChunk::getContentText));
                        StringBuilder existingFormulaText = new StringBuilder();
                        for (ContentChunk ec : existingChunks) {
                            existingFormulaText.append(ec.getContentText()).append(" ");
                        }
                        if (existingFormulaText.length() > 0) {
                            BigDecimal sim = calculateSimilarity(formulaHash,
                                    computeSimHash(existingFormulaText.toString()));
                            if (sim.compareTo(formulaSimilarity) > 0) {
                                formulaSimilarity = sim;
                            }
                        }
                    }
                }

                log.info("SimHash表格/公式相似度计算完成，tableSimilarity: {}, formulaSimilarity: {}", tableSimilarity,
                        formulaSimilarity);
            } catch (Exception e) {
                log.warn("SimHash计算表格/公式相似度失败，文档ID: {}", documentId, e);
            }

            // 6. 更新文档的相似度信息
            updateDocumentSimilarity(documentId, maxSimilarity, textSimilarity, tableSimilarity, formulaSimilarity,
                    similarDocs);

            // 7. 返回结果
            SimilarityResultDTO result = new SimilarityResultDTO();
            result.setDocumentId(documentId);
            result.setOverallSimilarity(maxSimilarity);
            result.setTextSimilarity(textSimilarity);
            result.setTableSimilarity(tableSimilarity);
            result.setFormulaSimilarity(formulaSimilarity);
            result.setSimilarDocuments(similarDocs);
            result.setDetectionTime(LocalDateTime.now());

            return result;

        } catch (Exception e) {
            log.error("使用AI向量计算相似度失败，文档ID: {}", documentId, e);
            return null;
        }
    }

    /**
     * 使用SimHash计算相似度（备用方案）
     */
    private SimilarityResultDTO checkSimilarityWithSimHash(Long documentId, String content) {
        try {
            // 1. 计算当前文档的SimHash
            long currentHash = computeSimHash(content);

            // 2. 分批次获取已有文档进行比对（排除自己）
            List<Document> existingDocs = getExistingDocuments(documentId);

            // 3. 计算文档级相似度
            List<SimilarDocumentDTO> similarDocs = new ArrayList<>();
            BigDecimal maxSimilarity = BigDecimal.ZERO;
            BigDecimal textSimilarity = BigDecimal.ZERO;

            List<List<Document>> batches = splitIntoBatches(existingDocs, BATCH_SIZE);
            for (List<Document> batch : batches) {
                List<SimilarDocumentDTO> batchSimilarDocs = processBatchWithSimHash(documentId, currentHash, batch);
                similarDocs.addAll(batchSimilarDocs);

                for (SimilarDocumentDTO similarDoc : batchSimilarDocs) {
                    if (similarDoc.getSimilarityScore().compareTo(maxSimilarity) > 0) {
                        maxSimilarity = similarDoc.getSimilarityScore();
                        textSimilarity = similarDoc.getSimilarityScore();
                    }
                }
            }

            // 4. 用 SimHash 计算表格和公式相似度
            BigDecimal tableSimilarity = BigDecimal.ZERO;
            BigDecimal formulaSimilarity = BigDecimal.ZERO;

            try {
                List<ContentChunk> currentChunks = contentChunkMapper.selectList(
                        new LambdaQueryWrapper<ContentChunk>()
                                .eq(ContentChunk::getDocumentId, documentId));

                StringBuilder tableText = new StringBuilder();
                StringBuilder formulaText = new StringBuilder();
                for (ContentChunk chunk : currentChunks) {
                    String chunkText = chunk.getContentText();
                    if (chunkText == null || chunkText.trim().isEmpty())
                        continue;
                    if ("TABLE".equals(chunk.getContentType())) {
                        tableText.append(chunkText).append(" ");
                    } else if ("FORMULA".equals(chunk.getContentType())) {
                        formulaText.append(chunkText).append(" ");
                    }
                }

                if (tableText.length() > 0) {
                    long tableHash = computeSimHash(tableText.toString());
                    for (Document existingDoc : existingDocs) {
                        List<ContentChunk> existingChunks = contentChunkMapper.selectList(
                                new LambdaQueryWrapper<ContentChunk>()
                                        .eq(ContentChunk::getDocumentId, existingDoc.getId())
                                        .eq(ContentChunk::getContentType, "TABLE")
                                        .isNotNull(ContentChunk::getContentText));
                        StringBuilder existingTableText = new StringBuilder();
                        for (ContentChunk ec : existingChunks) {
                            existingTableText.append(ec.getContentText()).append(" ");
                        }
                        if (existingTableText.length() > 0) {
                            BigDecimal sim = calculateSimilarity(tableHash,
                                    computeSimHash(existingTableText.toString()));
                            if (sim.compareTo(tableSimilarity) > 0) {
                                tableSimilarity = sim;
                            }
                        }
                    }
                }

                if (formulaText.length() > 0) {
                    long formulaHash = computeSimHash(formulaText.toString());
                    for (Document existingDoc : existingDocs) {
                        List<ContentChunk> existingChunks = contentChunkMapper.selectList(
                                new LambdaQueryWrapper<ContentChunk>()
                                        .eq(ContentChunk::getDocumentId, existingDoc.getId())
                                        .eq(ContentChunk::getContentType, "FORMULA")
                                        .isNotNull(ContentChunk::getContentText));
                        StringBuilder existingFormulaText = new StringBuilder();
                        for (ContentChunk ec : existingChunks) {
                            existingFormulaText.append(ec.getContentText()).append(" ");
                        }
                        if (existingFormulaText.length() > 0) {
                            BigDecimal sim = calculateSimilarity(formulaHash,
                                    computeSimHash(existingFormulaText.toString()));
                            if (sim.compareTo(formulaSimilarity) > 0) {
                                formulaSimilarity = sim;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("SimHash计算表格/公式相似度失败，文档ID: {}", documentId, e);
            }

            // 5. 更新文档的相似度信息
            updateDocumentSimilarity(documentId, maxSimilarity, textSimilarity, tableSimilarity, formulaSimilarity,
                    similarDocs);

            // 6. 返回结果
            SimilarityResultDTO result = new SimilarityResultDTO();
            result.setDocumentId(documentId);
            result.setOverallSimilarity(maxSimilarity);
            result.setTextSimilarity(textSimilarity);
            result.setTableSimilarity(tableSimilarity);
            result.setFormulaSimilarity(formulaSimilarity);
            result.setSimilarDocuments(similarDocs);
            result.setDetectionTime(LocalDateTime.now());

            return result;

        } catch (Exception e) {
            log.error("使用SimHash计算相似度失败，文档ID: {}", documentId, e);
            throw new BusinessException("重复率检测失败: " + e.getMessage());
        }
    }

    /**
     * 计算SimHash
     */
    private long computeSimHash(String content) {
        if (content == null || content.trim().isEmpty()) {
            return 0L;
        }

        String[] words = content.toLowerCase().split("\\s+");
        int[] vector = new int[HASH_BITS];

        for (String word : words) {
            if (word.length() < 2)
                continue;

            long wordHash = hash(word);
            for (int i = 0; i < HASH_BITS; i++) {
                if (((wordHash >> i) & 1) == 1) {
                    vector[i] += 1;
                } else {
                    vector[i] -= 1;
                }
            }
        }

        long simHash = 0;
        for (int i = 0; i < HASH_BITS; i++) {
            if (vector[i] > 0) {
                simHash |= (1L << i);
            }
        }

        return simHash;
    }

    /**
     * 计算汉明距离相似度
     */
    private BigDecimal calculateSimilarity(long hash1, long hash2) {
        long xor = hash1 ^ hash2;
        int distance = Long.bitCount(xor);
        double similarity = 1.0 - (double) distance / HASH_BITS;
        return BigDecimal.valueOf(similarity).setScale(4, BigDecimal.ROUND_HALF_UP);
    }

    private long hash(String str) {
        return Math.abs(str.hashCode());
    }

    /**
     * 保存相似度记录到你现有的Similarity表
     */
    private void saveSimilarityRecord(Long sourceDocId, Long targetDocId, BigDecimal similarity, String algorithm) {
        LambdaQueryWrapper<Similarity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Similarity::getSourceDocId, sourceDocId)
                .eq(Similarity::getTargetDocId, targetDocId)
                .eq(Similarity::getSimilarityType, "TEXT");

        Similarity existing = similarityMapper.selectOne(wrapper);
        if (existing == null) {
            Similarity record = new Similarity();
            record.setSourceDocId(sourceDocId);
            record.setTargetDocId(targetDocId);
            record.setSimilarityType("TEXT");
            record.setSimilarityScore(similarity);
            record.setAlgorithmUsed(algorithm);
            record.setCreateTime(LocalDateTime.now());
            similarityMapper.insert(record);
        } else if (similarity.compareTo(existing.getSimilarityScore()) > 0) {
            existing.setSimilarityScore(similarity);
            existing.setAlgorithmUsed(algorithm);
            similarityMapper.updateById(existing);
        }
    }

    /**
     * 更新文档的相似度信息
     */
    private void updateDocumentSimilarity(Long documentId, BigDecimal overallSimilarity, BigDecimal textSimilarity,
            BigDecimal tableSimilarity, BigDecimal formulaSimilarity,
            List<SimilarDocumentDTO> similarDocs) {
        try {
            log.info(
                    "开始更新文档相似度，文档ID: {}, overallSimilarity: {}, textSimilarity: {}, tableSimilarity: {}, formulaSimilarity: {}",
                    documentId, overallSimilarity, textSimilarity, tableSimilarity, formulaSimilarity);

            Document updateDoc = new Document();
            updateDoc.setId(documentId);
            updateDoc.setOverallSimilarity(overallSimilarity);
            updateDoc.setTextSimilarity(textSimilarity);
            updateDoc.setTableSimilarity(tableSimilarity);
            updateDoc.setFormulaSimilarity(formulaSimilarity);
            updateDoc.setIsDuplicate(overallSimilarity.compareTo(SIMILARITY_THRESHOLD) > 0 ? (byte) 1 : (byte) 0);
            updateDoc.setUpdateTime(LocalDateTime.now());

            int rows = documentMapper.updateById(updateDoc);

            log.info("文档相似度更新完成，文档ID: {}, 影响行数: {}", documentId, rows);
        } catch (Exception e) {
            log.error("更新文档相似度失败，文档ID: {}", documentId, e);
            throw e;
        }
    }

    @Override
    public List<SimilarityResultDTO> batchCheckSimilarity(List<Long> documentIds) {
        return documentIds.stream()
                .map(id -> {
                    Document doc = documentMapper.selectById(id);
                    return doc != null ? checkSimilarity(id, doc.getParsedContent()) : null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<SimilarDocumentDTO> getSimilarDocuments(Long documentId) {
        List<Similarity> similarities = similarityMapper.selectList(
                new LambdaQueryWrapper<Similarity>()
                        .eq(Similarity::getSourceDocId, documentId)
                        .orderByDesc(Similarity::getSimilarityScore));

        return similarities.stream()
                .map(this::convertToSimilarDocumentDTO)
                .collect(Collectors.toList());
    }

    private SimilarDocumentDTO convertToSimilarDocumentDTO(Similarity similarity) {
        Document targetDoc = documentMapper.selectById(similarity.getTargetDocId());

        SimilarDocumentDTO dto = new SimilarDocumentDTO();
        dto.setTargetDocumentId(similarity.getTargetDocId());
        dto.setTargetTitle(targetDoc != null ? targetDoc.getTitle() : "未知文档");
        dto.setSimilarityScore(similarity.getSimilarityScore());
        dto.setSimilarityType(similarity.getSimilarityType());
        return dto;
    }

    /**
     * 获取已有文档（排除当前文档）
     */
    private List<Document> getExistingDocuments(Long documentId) {
        return documentMapper.selectList(
                new LambdaQueryWrapper<Document>()
                        .ne(Document::getId, documentId));
    }

    /**
     * 将列表分成多个批次
     */
    private <T> List<List<T>> splitIntoBatches(List<T> list, int batchSize) {
        List<List<T>> batches = new ArrayList<>();
        for (int i = 0; i < list.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, list.size());
            batches.add(list.subList(i, endIndex));
        }
        return batches;
    }

    /**
     * 处理批次文档，使用向量计算相似度
     */
    private List<SimilarDocumentDTO> processBatchWithVector(Long documentId, List<Double> currentVector,
            List<Document> batch) {
        List<SimilarDocumentDTO> similarDocs = new ArrayList<>();
        int skippedDocs = 0;

        for (Document existingDoc : batch) {
            // 尝试从缓存获取向量
            String vectorCacheKey = "doc_vector:" + existingDoc.getId();
            String vectorStr = null;
            try {
                vectorStr = (String) redisTemplate.opsForValue().get(vectorCacheKey);
            } catch (Exception e) {
                log.warn("从Redis读取向量缓存失败，文档ID: {}, 错误: {}", existingDoc.getId(), e.getMessage());
                // 删除有问题的缓存
                try {
                    redisTemplate.delete(vectorCacheKey);
                } catch (Exception deleteEx) {
                    log.warn("删除有问题的缓存失败", deleteEx);
                }
                // 继续处理下一个
                vectorStr = null;
            }

            List<Double> existingVectorList = null;
            if (vectorStr != null && !vectorStr.trim().isEmpty()) {
                // 清理可能的控制字符
                vectorStr = vectorStr.replaceAll("\\x00", "").trim();
                if (!vectorStr.isEmpty()) {
                    // 从缓存解析向量
                    try {
                        List<?> rawVectorList = com.alibaba.fastjson.JSON.parseArray(vectorStr);
                        existingVectorList = new java.util.ArrayList<>();
                        for (Object item : rawVectorList) {
                            if (item instanceof Number) {
                                existingVectorList.add(((Number) item).doubleValue());
                            }
                        }
                    } catch (Exception e) {
                        log.warn("解析缓存向量失败，文档ID: {}, 删除有问题的缓存", existingDoc.getId(), e);
                        // 删除有问题的缓存
                        try {
                            redisTemplate.delete(vectorCacheKey);
                        } catch (Exception deleteEx) {
                            log.warn("删除缓存失败", deleteEx);
                        }
                        existingVectorList = null;
                    }
                }
            }

            // 如果缓存解析失败或不存在，从数据库获取
            if (existingVectorList == null || existingVectorList.isEmpty()) {
                // 从数据库获取向量
                com.taoxier.smartdochub.document.model.entity.DocumentVector existingVector = documentVectorService
                        .getByDocumentId(existingDoc.getId());
                if (existingVector == null || existingVector.getVector() == null) {
                    skippedDocs++;
                    continue;
                }

                try {
                    List<?> rawVectorList = com.alibaba.fastjson.JSON.parseArray(
                            existingVector.getVector());
                    if (rawVectorList == null || rawVectorList.isEmpty()) {
                        skippedDocs++;
                        continue;
                    }

                    existingVectorList = new java.util.ArrayList<>();
                    for (Object item : rawVectorList) {
                        if (item instanceof Number) {
                            existingVectorList.add(((Number) item).doubleValue());
                        } else {
                            log.warn("向量元素不是数字类型，跳过文档ID: {}", existingDoc.getId());
                            existingVectorList = null;
                            break;
                        }
                    }

                    if (existingVectorList != null && !existingVectorList.isEmpty()) {
                        // 缓存向量
                        try {
                            redisTemplate.opsForValue().set(vectorCacheKey, existingVector.getVector(),
                                    CACHE_EXPIRE_TIME);
                        } catch (Exception e) {
                            log.warn("缓存向量失败，文档ID: {}", existingDoc.getId(), e);
                        }
                    }
                } catch (Exception e) {
                    log.warn("解析向量失败，文档ID: {}", existingDoc.getId(), e);
                    existingVectorList = null;
                    skippedDocs++;
                    continue;
                }
            }

            if (existingVectorList == null || existingVectorList.isEmpty()) {
                skippedDocs++;
                continue;
            }

            // 计算余弦相似度
            double similarity = sentenceVectorService.calculateCosineSimilarity(currentVector, existingVectorList);
            BigDecimal similarityScore = BigDecimal.valueOf(similarity).setScale(4, BigDecimal.ROUND_HALF_UP);

            if (similarityScore.compareTo(new BigDecimal("0.1")) > 0) {
                SimilarDocumentDTO similarDoc = new SimilarDocumentDTO();
                similarDoc.setTargetDocumentId(existingDoc.getId());
                similarDoc.setTargetTitle(existingDoc.getTitle());
                similarDoc.setSimilarityScore(similarityScore);
                similarDoc.setSimilarityType("VECTOR");
                similarDocs.add(similarDoc);

                // 保存到相似度关联表
                saveSimilarityRecord(documentId, existingDoc.getId(), similarityScore, "VECTOR");
            }
        }

        if (skippedDocs > 0) {
            log.info("批次处理完成，跳过 {} 个没有向量的文档", skippedDocs);
        }

        return similarDocs;
    }

    /**
     * 处理批次文档，使用SimHash计算相似度
     */
    private List<SimilarDocumentDTO> processBatchWithSimHash(Long documentId, long currentHash, List<Document> batch) {
        List<SimilarDocumentDTO> similarDocs = new ArrayList<>();

        for (Document existingDoc : batch) {
            long existingHash = computeSimHash(existingDoc.getParsedContent());
            BigDecimal similarity = calculateSimilarity(currentHash, existingHash);

            if (similarity.compareTo(new BigDecimal("0.1")) > 0) {
                SimilarDocumentDTO similarDoc = new SimilarDocumentDTO();
                similarDoc.setTargetDocumentId(existingDoc.getId());
                similarDoc.setTargetTitle(existingDoc.getTitle());
                similarDoc.setSimilarityScore(similarity);
                similarDoc.setSimilarityType("TEXT");
                similarDocs.add(similarDoc);

                // 保存到相似度关联表
                saveSimilarityRecord(documentId, existingDoc.getId(), similarity, "SimHash");
            }
        }

        return similarDocs;
    }
}