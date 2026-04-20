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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private static final int HASH_BITS = 64;
    private static final BigDecimal SIMILARITY_THRESHOLD = new BigDecimal("0.6");

    @Override
    @Async("taskExecutor")
    public SimilarityResultDTO checkSimilarity(Long documentId, String content) {
        try {
            log.info("开始重复率检测，文档ID: {}", documentId);

            // 1. 计算当前文档的SimHash
            long currentHash = computeSimHash(content);

            // 2. 获取已有文档进行比对（排除自己）
            List<Document> existingDocs = documentMapper.selectList(
                    new LambdaQueryWrapper<Document>()
                            .ne(Document::getId, documentId)
                            .isNotNull(Document::getParsedContent)
                            .last("LIMIT 100"));

            // 3. 计算相似度
            List<SimilarDocumentDTO> similarDocs = new ArrayList<>();
            BigDecimal maxSimilarity = BigDecimal.ZERO;
            BigDecimal textSimilarity = BigDecimal.ZERO;
            BigDecimal tableSimilarity = BigDecimal.ZERO;
            BigDecimal formulaSimilarity = BigDecimal.ZERO;

            for (Document existingDoc : existingDocs) {
                long existingHash = computeSimHash(existingDoc.getParsedContent());
                BigDecimal similarity = calculateSimilarity(currentHash, existingHash);

                if (similarity.compareTo(new BigDecimal("0.1")) > 0) {
                    SimilarDocumentDTO similarDoc = new SimilarDocumentDTO();
                    similarDoc.setTargetDocumentId(existingDoc.getId());
                    similarDoc.setTargetTitle(existingDoc.getTitle());
                    similarDoc.setSimilarityScore(similarity);
                    similarDoc.setSimilarityType("TEXT");
                    similarDocs.add(similarDoc);

                    // 保存到相似度关联表（使用你现有的Similarity实体）
                    saveSimilarityRecord(documentId, existingDoc.getId(), similarity);
                }

                if (similarity.compareTo(maxSimilarity) > 0) {
                    maxSimilarity = similarity;
                    textSimilarity = similarity;
                }
            }

            // 4. 检查内容分块的相似度
            List<ContentChunk> currentChunks = contentChunkMapper.selectList(
                    new LambdaQueryWrapper<ContentChunk>()
                            .eq(ContentChunk::getDocumentId, documentId));

            for (ContentChunk chunk : currentChunks) {
                if (chunk.getContentText() != null && !chunk.getContentText().isEmpty()) {
                    // 计算每个内容块的相似度
                    BigDecimal chunkSimilarity = checkChunkSimilarity(chunk);
                    if ("TABLE".equals(chunk.getContentType()) && chunkSimilarity.compareTo(tableSimilarity) > 0) {
                        tableSimilarity = chunkSimilarity;
                    } else if ("FORMULA".equals(chunk.getContentType())
                            && chunkSimilarity.compareTo(formulaSimilarity) > 0) {
                        formulaSimilarity = chunkSimilarity;
                    }
                }
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

            log.info("重复率检测完成，文档ID: {}, 最高相似度: {}", documentId, maxSimilarity);
            return result;

        } catch (Exception e) {
            log.error("重复率检测失败，文档ID: {}", documentId, e);
            throw new BusinessException("重复率检测失败: " + e.getMessage());
        }
    }

    /**
     * 检查单个内容块的相似度
     */
    private BigDecimal checkChunkSimilarity(ContentChunk chunk) {
        try {
            long chunkHash = computeSimHash(chunk.getContentText());
            List<ContentChunk> existingChunks = contentChunkMapper.selectList(
                    new LambdaQueryWrapper<ContentChunk>()
                            .ne(ContentChunk::getDocumentId, chunk.getDocumentId())
                            .eq(ContentChunk::getContentType, chunk.getContentType())
                            .isNotNull(ContentChunk::getContentText)
                            .last("LIMIT 50"));

            BigDecimal maxChunkSimilarity = BigDecimal.ZERO;
            for (ContentChunk existingChunk : existingChunks) {
                long existingChunkHash = computeSimHash(existingChunk.getContentText());
                BigDecimal similarity = calculateSimilarity(chunkHash, existingChunkHash);
                if (similarity.compareTo(maxChunkSimilarity) > 0) {
                    maxChunkSimilarity = similarity;
                }
            }

            // 更新内容块的相似度
            ContentChunk updateChunk = new ContentChunk();
            updateChunk.setId(chunk.getId());
            updateChunk.setSimilarityScore(maxChunkSimilarity);
            contentChunkMapper.updateById(updateChunk);

            return maxChunkSimilarity;
        } catch (Exception e) {
            log.error("检查内容块相似度失败，块ID: {}", chunk.getId(), e);
            return BigDecimal.ZERO;
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
    private void saveSimilarityRecord(Long sourceDocId, Long targetDocId, BigDecimal similarity) {
        Similarity record = new Similarity();
        record.setSourceDocId(sourceDocId);
        record.setTargetDocId(targetDocId);
        record.setSimilarityType("TEXT");
        record.setSimilarityScore(similarity);
        record.setAlgorithmUsed("SimHash");
        record.setCreateTime(LocalDateTime.now());

        similarityMapper.insert(record);
    }

    /**
     * 更新文档的相似度信息
     */
    private void updateDocumentSimilarity(Long documentId, BigDecimal overallSimilarity, BigDecimal textSimilarity,
            BigDecimal tableSimilarity, BigDecimal formulaSimilarity,
            List<SimilarDocumentDTO> similarDocs) {
        Document updateDoc = new Document();
        updateDoc.setId(documentId);
        updateDoc.setOverallSimilarity(overallSimilarity);
        updateDoc.setTextSimilarity(textSimilarity);
        updateDoc.setTableSimilarity(tableSimilarity);
        updateDoc.setFormulaSimilarity(formulaSimilarity);
        updateDoc.setIsDuplicate(overallSimilarity.compareTo(SIMILARITY_THRESHOLD) > 0 ? (byte) 1 : (byte) 0);
        updateDoc.setUpdateTime(LocalDateTime.now());

        documentMapper.updateById(updateDoc);
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
}