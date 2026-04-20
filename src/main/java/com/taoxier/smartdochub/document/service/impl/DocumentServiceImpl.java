package com.taoxier.smartdochub.document.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taoxier.smartdochub.common.exception.BusinessException;
import com.taoxier.smartdochub.document.mapper.AsyncTaskMapper;
import com.taoxier.smartdochub.document.mapper.DocumentVersionMapper;
import com.taoxier.smartdochub.document.model.dto.DocumentQueryDTO;
import com.taoxier.smartdochub.document.model.dto.DocumentUpdateDTO;
import com.taoxier.smartdochub.document.model.entity.ContentChunk;
import com.taoxier.smartdochub.document.model.entity.Document;
import com.taoxier.smartdochub.document.model.entity.DocumentVersion;
import com.taoxier.smartdochub.document.model.entity.Topic;
import com.taoxier.smartdochub.document.mapper.DocumentMapper;
import com.taoxier.smartdochub.document.model.vo.DocumentStatsVO;
import com.taoxier.smartdochub.document.service.ContentChunkService;
import com.taoxier.smartdochub.document.service.DocumentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taoxier.smartdochub.document.service.TopicService;
import com.taoxier.smartdochub.ai.service.AIService;
import com.taoxier.smartdochub.document.service.UserBehaviorService;
import com.taoxier.smartdochub.document.service.SimilarityDetectionService;
import com.taoxier.smartdochub.document.service.KgService;
import com.taoxier.smartdochub.document.service.AiDetectionService;
import com.taoxier.smartdochub.document.util.parse.DocumentParser;
import com.taoxier.smartdochub.document.util.parse.DocumentParseResult;
import com.taoxier.smartdochub.document.model.vo.DocumentDetailVO;
import com.taoxier.smartdochub.document.model.vo.DocumentVO;
import com.taoxier.smartdochub.document.mapper.UserBehaviorMapper;
import com.taoxier.smartdochub.file.model.FileInfo;
import com.taoxier.smartdochub.file.service.FileService;
import com.taoxier.smartdochub.system.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl extends ServiceImpl<DocumentMapper, Document> implements DocumentService {

    private final FileService fileService;
    private final ContentChunkService contentChunkService;
    private final TopicService topicService;
    private final DocumentParser documentParser;
    private final UserService userService;
    private final UserBehaviorService userBehaviorService;
    private final UserBehaviorMapper userBehaviorMapper;
    private final AIService aiService;
    private final DocumentVersionMapper documentVersionMapper;
    private final AsyncTaskMapper asyncTaskMapper;
    private final SimilarityDetectionService similarityDetectionService;
    private final KgService kgService;
    private final AiDetectionService aiDetectionService;

    @Override
    @Transactional
    public Document uploadAndParse(MultipartFile file, Long userId) {
        try {
            String extName = FileUtil.extName(file.getOriginalFilename());
            File tempFile = File.createTempFile("doc-", "." + extName);
            file.transferTo(tempFile);

            String fileHash = DigestUtil.sha256Hex(tempFile);

            Document existingDoc = checkDuplicateByHash(fileHash, userId);
            if (existingDoc != null) {
                FileUtil.del(tempFile);
                throw new BusinessException("文档已存在，重复文档ID: " + existingDoc.getId());
            }

            // 先解析文档
            DocumentParseResult parseResult;
            try (InputStream tempInputStream = new FileInputStream(tempFile)) {
                parseResult = documentParser.parse(tempInputStream, extName);
            }

            // 然后上传文件
            FileInfo fileInfo = fileService.uploadFile(new MultipartFile() {
                @Override
                public String getName() {
                    return file.getName();
                }

                @Override
                public String getOriginalFilename() {
                    return file.getOriginalFilename();
                }

                @Override
                public String getContentType() {
                    return file.getContentType();
                }

                @Override
                public boolean isEmpty() {
                    return file.isEmpty();
                }

                @Override
                public long getSize() {
                    return file.getSize();
                }

                @Override
                public byte[] getBytes() throws IOException {
                    return Files.readAllBytes(tempFile.toPath());
                }

                @Override
                public InputStream getInputStream() throws IOException {
                    return new FileInputStream(tempFile);
                }

                @Override
                public void transferTo(File dest) throws IOException, IllegalStateException {
                    Files.copy(tempFile.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            });

            Document document = saveDocumentRecord(file, fileInfo, userId, parseResult, fileHash);

            // 创建初始版本记录
            DocumentVersion version = createDocumentVersion(document, parseResult);
            documentVersionMapper.insert(version);

            // 保存内容分块，添加版本号
            parseResult.getChunks().forEach(chunk -> {
                chunk.setDocumentId(document.getId());
                chunk.setVersionNumber(1);
                chunk.setCreateTime(LocalDateTime.now());
            });
            contentChunkService.saveBatch(parseResult.getChunks());

            // 异步进行相似度检测
            CompletableFuture.runAsync(() -> {
                try {
                    similarityDetectionService.checkSimilarity(document.getId(), parseResult.getFullText());
                } catch (Exception e) {
                    log.error("相似度检测失败，文档ID: " + document.getId(), e);
                }
            });

            // 异步构建知识图谱（在Topic生成后触发）
            CompletableFuture.runAsync(() -> {
                try {
                    // 等待Topic生成完成
                    Thread.sleep(2000);
                    kgService.buildDocumentKnowledgeGraph(document.getId());
                } catch (Exception e) {
                    log.error("知识图谱构建失败，文档ID: " + document.getId(), e);
                }
            });

            // 异步进行AI生成检测
            CompletableFuture.runAsync(() -> {
                try {
                    double aiProbability = aiDetectionService.detectDocumentAiGeneration(document.getId(),
                            parseResult.getFullText());
                    // 更新文档的AI生成概率
                    Document updateDoc = new Document();
                    updateDoc.setId(document.getId());
                    updateDoc.setAiProbability(BigDecimal.valueOf(aiProbability));
                    this.updateById(updateDoc);
                } catch (Exception e) {
                    log.error("AI生成检测失败，文档ID: " + document.getId(), e);
                }
            });

            FileUtil.del(tempFile);

            return document;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("文档处理失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Document uploadNewVersion(MultipartFile file, Long documentId, Long userId) {
        try {
            // 检查文档是否存在
            Document document = this.getById(documentId);
            if (document == null || document.getIsDeleted() == 1) {
                throw new BusinessException("文档不存在");
            }

            // 检查是否是文档上传者
            if (!document.getUploadUserId().equals(userId)) {
                throw new BusinessException("只有文档上传者可以更新文档");
            }

            String extName = FileUtil.extName(file.getOriginalFilename());
            File tempFile = File.createTempFile("doc-", "." + extName);
            file.transferTo(tempFile);

            String fileHash = DigestUtil.sha256Hex(tempFile);

            // 先解析文档
            DocumentParseResult parseResult;
            try (InputStream tempInputStream = new FileInputStream(tempFile)) {
                parseResult = documentParser.parse(tempInputStream, extName);
            }

            // 然后上传文件
            FileInfo fileInfo = fileService.uploadFile(new MultipartFile() {
                @Override
                public String getName() {
                    return file.getName();
                }

                @Override
                public String getOriginalFilename() {
                    return file.getOriginalFilename();
                }

                @Override
                public String getContentType() {
                    return file.getContentType();
                }

                @Override
                public boolean isEmpty() {
                    return file.isEmpty();
                }

                @Override
                public long getSize() {
                    return file.getSize();
                }

                @Override
                public byte[] getBytes() throws IOException {
                    return Files.readAllBytes(tempFile.toPath());
                }

                @Override
                public InputStream getInputStream() throws IOException {
                    return new FileInputStream(tempFile);
                }

                @Override
                public void transferTo(File dest) throws IOException, IllegalStateException {
                    Files.copy(tempFile.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            });

            // 计算新版本号
            int newVersion = document.getCurrentVersion() + 1;
            document.setCurrentVersion(newVersion);
            document.setVersionCount(document.getVersionCount() + 1);
            document.setOriginalFilename(file.getOriginalFilename());
            document.setStoragePath(fileInfo.getUrl());
            document.setFileSize(file.getSize());
            document.setFileType(extName);
            document.setFileHash(fileHash);
            document.setProcessStatus("COMPLETED");
            document.setProcessProgress(100);
            document.setPageCount(parseResult.getPageCount());
            document.setWordCount(parseResult.getWordCount());
            document.setCharacterCount(parseResult.getCharacterCount());
            document.setParsedContent(parseResult.getFullText());
            document.setUpdateTime(LocalDateTime.now());

            // 更新文档主表
            this.updateById(document);

            // 创建新版本记录
            DocumentVersion version = createDocumentVersion(document, parseResult);
            documentVersionMapper.insert(version);

            // 保存内容分块，添加版本号
            parseResult.getChunks().forEach(chunk -> {
                chunk.setDocumentId(document.getId());
                chunk.setVersionNumber(newVersion);
                chunk.setCreateTime(LocalDateTime.now());
            });
            contentChunkService.saveBatch(parseResult.getChunks());

            // 使用AI生成文档描述、分类和标签
            String content = parseResult.getFullText();
            if (content != null && !content.isEmpty()) {
                // 生成文档分类
                String category = aiService.classifyDocument(content);
                if (category != null) {
                    document.setCategory(category);
                    this.updateById(document);
                }

                // 生成文档描述
                String description = aiService.generateDescription(content);
                if (description != null) {
                    document.setDescription(description);
                    this.updateById(document);
                }

                // 生成文档标签
                java.util.List<String> tags = aiService.generateTags(content);
                if (tags != null && !tags.isEmpty()) {
                    // 保存标签到topic表
                    for (String tag : tags) {
                        Topic topic = new Topic();
                        topic.setDocumentId(document.getId());
                        topic.setVersionNumber(newVersion);
                        topic.setTopicType("KEYWORD");
                        topic.setTopicValue(tag);
                        topic.setCreateTime(LocalDateTime.now());
                        topicService.save(topic);
                    }
                }
            }

            // 异步进行相似度检测
            CompletableFuture.runAsync(() -> {
                try {
                    similarityDetectionService.checkSimilarity(document.getId(), parseResult.getFullText());
                } catch (Exception e) {
                    log.error("相似度检测失败，文档ID: " + document.getId(), e);
                }
            });

            // 异步构建知识图谱（在Topic生成后触发）
            CompletableFuture.runAsync(() -> {
                try {
                    // 等待Topic生成完成
                    Thread.sleep(2000);
                    kgService.buildDocumentKnowledgeGraph(document.getId());
                } catch (Exception e) {
                    log.error("知识图谱构建失败，文档ID: " + document.getId(), e);
                }
            });

            // 异步进行AI生成检测
            CompletableFuture.runAsync(() -> {
                try {
                    double aiProbability = aiDetectionService.detectDocumentAiGeneration(document.getId(),
                            parseResult.getFullText());
                    // 更新文档的AI生成概率
                    Document updateDoc = new Document();
                    updateDoc.setId(document.getId());
                    updateDoc.setAiProbability(BigDecimal.valueOf(aiProbability));
                    this.updateById(updateDoc);
                } catch (Exception e) {
                    log.error("AI生成检测失败，文档ID: " + document.getId(), e);
                }
            });

            FileUtil.del(tempFile);

            return document;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("文档版本更新失败：" + e.getMessage());
        }
    }

    @Override
    public IPage<DocumentVO> queryDocumentPage(DocumentQueryDTO queryDTO, Long currentUserId) {
        Page<Document> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        LambdaQueryWrapper<Document> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(queryDTO.getTitle())) {
            wrapper.like(Document::getTitle, queryDTO.getTitle());
        }

        if (StringUtils.hasText(queryDTO.getFileType())) {
            wrapper.eq(Document::getFileType, queryDTO.getFileType());
        }

        if (queryDTO.getUploadUserId() != null) {
            wrapper.eq(Document::getUploadUserId, queryDTO.getUploadUserId());
        }

        if (queryDTO.getIsPublic() != null) {
            wrapper.eq(Document::getIsPublic, queryDTO.getIsPublic());
        }

        if (queryDTO.getIsDuplicate() != null) {
            wrapper.eq(Document::getIsDuplicate, queryDTO.getIsDuplicate());
        }

        if (queryDTO.getIsAiGenerated() != null) {
            wrapper.eq(Document::getIsAiGenerated, queryDTO.getIsAiGenerated());
        }

        if (StringUtils.hasText(queryDTO.getProcessStatus())) {
            wrapper.eq(Document::getProcessStatus, queryDTO.getProcessStatus());
        }

        if (StringUtils.hasText(queryDTO.getKeyword())) {
            wrapper.and(w -> w
                    .like(Document::getTitle, queryDTO.getKeyword())
                    .or()
                    .like(Document::getDescription, queryDTO.getKeyword())
                    .or()
                    .like(Document::getParsedContent, queryDTO.getKeyword()));
        }

        if (StringUtils.hasText(queryDTO.getCategory())) {
            wrapper.eq(Document::getCategory, queryDTO.getCategory());
        }

        if (StringUtils.hasText(queryDTO.getTags())) {
            String[] tagArray = queryDTO.getTags().split(",");
            String tagSql = "SELECT document_id FROM doc_topic WHERE topic_type = 'KEYWORD' AND topic_value IN ("
                    + Arrays.stream(tagArray).map(t -> "'" + t.trim() + "'").collect(Collectors.joining(","))
                    + ")";
            wrapper.inSql(Document::getId, tagSql);
        }

        wrapper.eq(Document::getIsDeleted, 0);

        if (currentUserId != null) {
            wrapper.and(w -> w
                    .eq(Document::getIsPublic, 1)
                    .or()
                    .eq(Document::getUploadUserId, currentUserId));
        } else {
            wrapper.eq(Document::getIsPublic, 1);
        }

        if (StringUtils.hasText(queryDTO.getSortBy())) {
            boolean isAsc = "asc".equalsIgnoreCase(queryDTO.getSortOrder());
            switch (queryDTO.getSortBy()) {
                case "createTime":
                    wrapper.orderBy(true, isAsc, Document::getCreateTime);
                    break;
                case "viewCount":
                    wrapper.orderBy(true, isAsc, Document::getViewCount);
                    break;
                case "downloadCount":
                    wrapper.orderBy(true, isAsc, Document::getDownloadCount);
                    break;
                default:
                    wrapper.orderByDesc(Document::getCreateTime);
            }
        } else {
            wrapper.orderByDesc(Document::getCreateTime);
        }

        IPage<Document> documentPage = this.page(page, wrapper);

        return documentPage.convert(this::convertToVO);
    }

    @Override
    public DocumentDetailVO getDocumentDetail(Long id, Long currentUserId) {
        Document document = this.getById(id);
        if (document == null || document.getIsDeleted() == 1) {
            throw new BusinessException("文档不存在");
        }

        if (document.getIsPublic() != 1
                && (currentUserId == null || !document.getUploadUserId().equals(currentUserId))) {
            throw new BusinessException("无权查看该文档");
        }

        incrementViewCount(id);

        if (currentUserId != null) {
            userBehaviorService.recordBehavior(currentUserId, id, "VIEW");
        }

        DocumentDetailVO detailVO = new DocumentDetailVO();
        BeanUtils.copyProperties(document, detailVO);

        String userName = getUserNameById(document.getUploadUserId());
        detailVO.setUploadUserName(userName);

        String previewUrl = fileService.getPreviewUrl(document.getStoragePath());
        detailVO.setPreviewUrl(previewUrl);

        List<ContentChunk> chunks = contentChunkService.list(
                new LambdaQueryWrapper<ContentChunk>()
                        .eq(ContentChunk::getDocumentId, id)
                        .eq(ContentChunk::getVersionNumber, document.getCurrentVersion())
                        .orderByAsc(ContentChunk::getChunkIndex));
        List<DocumentDetailVO.ContentChunkVO> chunkVOs = chunks.stream().map(chunk -> {
            DocumentDetailVO.ContentChunkVO chunkVO = new DocumentDetailVO.ContentChunkVO();
            BeanUtils.copyProperties(chunk, chunkVO);
            return chunkVO;
        }).collect(Collectors.toList());
        detailVO.setChunks(chunkVOs);

        List<Topic> topics = topicService.list(
                new LambdaQueryWrapper<Topic>()
                        .eq(Topic::getDocumentId, id)
                        .eq(Topic::getVersionNumber, document.getCurrentVersion()));
        List<DocumentDetailVO.TopicVO> topicVOs = topics.stream().map(topic -> {
            DocumentDetailVO.TopicVO topicVO = new DocumentDetailVO.TopicVO();
            BeanUtils.copyProperties(topic, topicVO);
            return topicVO;
        }).collect(Collectors.toList());
        detailVO.setTopics(topicVOs);

        return detailVO;
    }

    @Override
    public DocumentDetailVO getDocumentVersionDetail(Long id, Integer versionNumber, Long currentUserId) {
        Document document = this.getById(id);
        if (document == null || document.getIsDeleted() == 1) {
            throw new BusinessException("文档不存在");
        }

        if (document.getIsPublic() != 1 && !document.getUploadUserId().equals(currentUserId)) {
            throw new BusinessException("无权查看该文档");
        }

        // 检查版本是否存在
        DocumentVersion version = documentVersionMapper.selectOne(new QueryWrapper<DocumentVersion>()
                .eq("document_id", id)
                .eq("version_number", versionNumber));
        if (version == null) {
            throw new BusinessException("版本不存在");
        }

        DocumentDetailVO detailVO = new DocumentDetailVO();
        BeanUtils.copyProperties(version, detailVO);
        detailVO.setId(document.getId());

        String userName = getUserNameById(version.getUploadUserId());
        detailVO.setUploadUserName(userName);

        String previewUrl = fileService.getPreviewUrl(version.getStoragePath());
        detailVO.setPreviewUrl(previewUrl);

        List<ContentChunk> chunks = contentChunkService.list(
                new LambdaQueryWrapper<ContentChunk>()
                        .eq(ContentChunk::getDocumentId, id)
                        .eq(ContentChunk::getVersionNumber, versionNumber)
                        .orderByAsc(ContentChunk::getChunkIndex));
        List<DocumentDetailVO.ContentChunkVO> chunkVOs = chunks.stream().map(chunk -> {
            DocumentDetailVO.ContentChunkVO chunkVO = new DocumentDetailVO.ContentChunkVO();
            BeanUtils.copyProperties(chunk, chunkVO);
            return chunkVO;
        }).collect(Collectors.toList());
        detailVO.setChunks(chunkVOs);

        List<Topic> topics = topicService.list(
                new LambdaQueryWrapper<Topic>()
                        .eq(Topic::getDocumentId, id)
                        .eq(Topic::getVersionNumber, versionNumber));
        List<DocumentDetailVO.TopicVO> topicVOs = topics.stream().map(topic -> {
            DocumentDetailVO.TopicVO topicVO = new DocumentDetailVO.TopicVO();
            BeanUtils.copyProperties(topic, topicVO);
            return topicVO;
        }).collect(Collectors.toList());
        detailVO.setTopics(topicVOs);

        return detailVO;
    }

    @Override
    @Transactional
    public void deleteDocument(Long id, Long currentUserId) {
        Document document = this.getById(id);
        if (document == null || document.getIsDeleted() == 1) {
            throw new BusinessException("文档不存在");
        }

        if (!document.getUploadUserId().equals(currentUserId)) {
            throw new BusinessException("无权删除该文档");
        }

        document.setIsDeleted((byte) 1);
        document.setUpdateTime(LocalDateTime.now());
        this.updateById(document);

        fileService.deleteFile(document.getStoragePath());
    }

    @Override
    public void downloadDocument(Long id, Long currentUserId, HttpServletResponse response) {
        Document document = this.getById(id);
        if (document == null || document.getIsDeleted() == 1) {
            throw new BusinessException("文档不存在");
        }

        if (currentUserId == null) {
            throw new BusinessException("未授权访问");
        }

        if (document.getIsPublic() != 1 && !document.getUploadUserId().equals(currentUserId)) {
            throw new BusinessException("无权下载该文档");
        }

        incrementDownloadCount(id);

        userBehaviorService.recordBehavior(currentUserId, id, "DOWNLOAD");

        try {
            String filePath = document.getStoragePath();
            String fileName = document.getOriginalFilename();

            response.setContentType("application/octet-stream");
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"");

            // 使用COS客户端下载文件
            fileService.downloadFile(filePath, response.getOutputStream());
        } catch (Exception e) {
            throw new BusinessException("文件下载失败：" + e.getMessage());
        }
    }

    @Override
    public void downloadDocumentVersion(Long id, Integer versionNumber, Long currentUserId,
            HttpServletResponse response) {
        Document document = this.getById(id);
        if (document == null || document.getIsDeleted() == 1) {
            throw new BusinessException("文档不存在");
        }

        if (currentUserId == null) {
            throw new BusinessException("未授权访问");
        }

        if (document.getIsPublic() != 1 && !document.getUploadUserId().equals(currentUserId)) {
            throw new BusinessException("无权下载该文档");
        }

        // 检查版本是否存在
        DocumentVersion version = documentVersionMapper.selectOne(new QueryWrapper<DocumentVersion>()
                .eq("document_id", id)
                .eq("version_number", versionNumber));
        if (version == null) {
            throw new BusinessException("版本不存在");
        }

        incrementDownloadCount(id);

        userBehaviorService.recordBehavior(currentUserId, id, "DOWNLOAD");

        try {
            String filePath = version.getStoragePath();
            String fileName = version.getOriginalFilename();

            response.setContentType("application/octet-stream");
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"");

            // 使用COS客户端下载文件
            fileService.downloadFile(filePath, response.getOutputStream());
        } catch (Exception e) {
            throw new BusinessException("文件下载失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Document updateDocumentMetadata(DocumentUpdateDTO updateDTO, Long currentUserId) {
        Document document = this.getById(updateDTO.getId());
        if (document == null || document.getIsDeleted() == 1) {
            throw new BusinessException("文档不存在");
        }

        if (!document.getUploadUserId().equals(currentUserId)) {
            throw new BusinessException("无权修改该文档");
        }

        if (StringUtils.hasText(updateDTO.getTitle())) {
            document.setTitle(updateDTO.getTitle());
        }
        if (updateDTO.getDescription() != null) {
            document.setDescription(updateDTO.getDescription());
        }
        if (updateDTO.getIsPublic() != null) {
            document.setIsPublic(updateDTO.getIsPublic());
        }
        if (updateDTO.getCategory() != null) {
            document.setCategory(updateDTO.getCategory());
        }
        document.setUpdateTime(LocalDateTime.now());

        this.updateById(document);

        // 更新标签
        if (updateDTO.getTags() != null) {
            // 删除旧的标签
            topicService.remove(new LambdaQueryWrapper<Topic>()
                    .eq(Topic::getDocumentId, document.getId())
                    .eq(Topic::getVersionNumber, document.getCurrentVersion()));

            // 添加新的标签
            for (String tag : updateDTO.getTags()) {
                Topic topic = new Topic();
                topic.setDocumentId(document.getId());
                topic.setVersionNumber(document.getCurrentVersion());
                topic.setTopicType("KEYWORD");
                topic.setTopicValue(tag);
                topic.setCreateTime(LocalDateTime.now());
                topicService.save(topic);
            }
        }

        return document;
    }

    @Override
    @Transactional
    public void incrementViewCount(Long id) {
        baseMapper.incrementViewCount(id);
    }

    @Override
    @Transactional
    public void incrementDownloadCount(Long id) {
        baseMapper.incrementDownloadCount(id);
    }

    @Override
    public Document checkDuplicateByHash(String fileHash, Long userId) {
        return baseMapper.selectOne(
                new LambdaQueryWrapper<Document>()
                        .eq(Document::getFileHash, fileHash)
                        .eq(Document::getIsDeleted, 0));
    }

    @Override
    public DocumentStatsVO getDocumentStats() {
        DocumentStatsVO stats = new DocumentStatsVO();

        // 统计文档总数
        Integer totalDocs = Math.toIntExact(baseMapper.selectCount(
                new LambdaQueryWrapper<Document>()
                        .eq(Document::getIsDeleted, 0)));
        stats.setTotalDocs(totalDocs);

        // 统计总浏览量
        Integer totalViews = baseMapper.selectObjs(
                new LambdaQueryWrapper<Document>()
                        .eq(Document::getIsDeleted, 0)
                        .select(Document::getViewCount))
                .stream()
                .mapToInt(obj -> obj != null ? (Integer) obj : 0)
                .sum();
        stats.setTotalViews(totalViews);

        // 统计总下载量
        Integer totalDownloads = baseMapper.selectObjs(
                new LambdaQueryWrapper<Document>()
                        .eq(Document::getIsDeleted, 0)
                        .select(Document::getDownloadCount))
                .stream()
                .mapToInt(obj -> obj != null ? (Integer) obj : 0)
                .sum();
        stats.setTotalDownloads(totalDownloads);

        // 统计总收藏量
        Integer totalFavorites = baseMapper.selectObjs(
                new LambdaQueryWrapper<Document>()
                        .eq(Document::getIsDeleted, 0)
                        .select(Document::getFavoriteCount))
                .stream()
                .mapToInt(obj -> obj != null ? (Integer) obj : 0)
                .sum();
        stats.setTotalFavorites(totalFavorites);

        // 计算平均评分（从 user_behavior 表中获取）
        // 这里简化处理，实际应该从 user_behavior 表中查询
        stats.setAvgRating(BigDecimal.ZERO);

        return stats;
    }

    private Document saveDocumentRecord(MultipartFile file, FileInfo fileInfo, Long userId,
            DocumentParseResult parseResult, String fileHash) {
        Document document = new Document();
        document.setTitle(FileUtil.mainName(file.getOriginalFilename()));
        document.setOriginalFilename(file.getOriginalFilename());
        document.setStoragePath(fileInfo.getUrl());
        document.setFileType(FileUtil.extName(file.getOriginalFilename()));
        document.setFileSize(file.getSize());
        document.setFileHash(fileHash);
        document.setUploadUserId(userId);
        document.setProcessStatus("COMPLETED");
        document.setProcessProgress(100);
        document.setPageCount(parseResult.getPageCount());
        document.setWordCount(parseResult.getWordCount());
        document.setCharacterCount(parseResult.getCharacterCount());
        document.setParsedContent(parseResult.getFullText());
        document.setCreateTime(LocalDateTime.now());
        document.setUpdateTime(LocalDateTime.now());
        document.setIsPublic((byte) 1);
        document.setIsDeleted((byte) 0);
        document.setViewCount(0);
        document.setDownloadCount(0);
        document.setFavoriteCount(0);
        document.setCurrentVersion(1);
        document.setVersionCount(1);

        // 先保存文档，获取id
        this.save(document);

        // 使用AI生成文档描述、分类和标签
        String content = parseResult.getFullText();
        if (content != null && !content.isEmpty()) {
            // 生成文档描述
            String description = aiService.generateDescription(content);
            if (description != null) {
                document.setDescription(description);
            }

            // 生成文档分类
            String category = aiService.classifyDocument(content);
            if (category != null) {
                document.setCategory(category);
            }

            // 更新文档信息
            this.updateById(document);

            // 生成文档标签
            java.util.List<String> tags = aiService.generateTags(content);
            if (tags != null && !tags.isEmpty()) {
                // 保存标签到topic表
                for (String tag : tags) {
                    Topic topic = new Topic();
                    topic.setDocumentId(document.getId());
                    topic.setVersionNumber(1);
                    topic.setTopicType("KEYWORD");
                    topic.setTopicValue(tag);
                    topic.setCreateTime(LocalDateTime.now());
                    topicService.save(topic);
                }
            }
        }

        return document;
    }

    private DocumentVO convertToVO(Document document) {
        DocumentVO vo = new DocumentVO();
        BeanUtils.copyProperties(document, vo);
        vo.setUploadUserName(getUserNameById(document.getUploadUserId()));

        // 获取文档标签
        List<Topic> topics = topicService.list(
                new LambdaQueryWrapper<Topic>()
                        .eq(Topic::getDocumentId, document.getId())
                        .eq(Topic::getVersionNumber, document.getCurrentVersion()));
        vo.setTopics(topics);

        return vo;
    }

    private String getUserNameById(Long userId) {
        if (userId == null) {
            return "未知用户";
        }
        try {
            var user = userService.getById(userId);
            return user != null ? user.getNickname() : "未知用户";
        } catch (Exception e) {
            return "未知用户";
        }
    }

    private DocumentVersion createDocumentVersion(Document document, DocumentParseResult parseResult) {
        DocumentVersion version = new DocumentVersion();
        version.setDocumentId(document.getId());
        version.setVersionNumber(document.getCurrentVersion());
        version.setTitle(document.getTitle());
        version.setDescription(document.getDescription());
        version.setCategory(document.getCategory());
        version.setOriginalFilename(document.getOriginalFilename());
        version.setStoragePath(document.getStoragePath());
        version.setFileSize(document.getFileSize());
        version.setFileType(document.getFileType());
        version.setFileHash(document.getFileHash());
        version.setUploadUserId(document.getUploadUserId());
        version.setProcessStatus(document.getProcessStatus());
        version.setProcessProgress(document.getProcessProgress());
        version.setProcessMessage(document.getProcessMessage());
        version.setPageCount(document.getPageCount());
        version.setWordCount(document.getWordCount());
        version.setCharacterCount(document.getCharacterCount());
        version.setParsedContent(document.getParsedContent());
        version.setOverallSimilarity(document.getOverallSimilarity());
        version.setTextSimilarity(document.getTextSimilarity());
        version.setTableSimilarity(document.getTableSimilarity());
        version.setFormulaSimilarity(document.getFormulaSimilarity());
        version.setAiProbability(document.getAiProbability());
        version.setDetectedAiModel(document.getDetectedAiModel());
        version.setQualityScore(document.getQualityScore());
        version.setReadabilityScore(document.getReadabilityScore());
        version.setIsDuplicate(document.getIsDuplicate());
        version.setIsAiGenerated(document.getIsAiGenerated());
        version.setCreateTime(LocalDateTime.now());
        return version;
    }

    @Override
    public void updateFavoriteCount(Long documentId) {
        // 统计该文档的收藏次数
        int count = userBehaviorService.countByDocumentAndType(documentId, "FAVORITE");
        // 更新文档的收藏计数
        Document document = new Document();
        document.setId(documentId);
        document.setFavoriteCount(count);
        this.updateById(document);
    }

    @Override
    public void removeFavorite(Long userId, Long documentId) {
        // 删除用户对该文档的收藏记录
        userBehaviorService.removeByUserAndDocumentAndType(userId, documentId, "FAVORITE");
    }

    @Override
    public List<DocumentVersion> getDocumentVersions(Long documentId) {
        return documentVersionMapper.selectList(new QueryWrapper<DocumentVersion>()
                .eq("document_id", documentId)
                .orderByDesc("version_number"));
    }

    @Override
    public String generateShareUrl(Long documentId) {
        // 生成分享令牌
        String token = java.util.UUID.randomUUID().toString().replace("-", "");
        // 存储分享令牌到Redis或数据库，这里简化处理
        // 实际项目中应该存储令牌和文档ID的映射关系，并设置过期时间
        // 生成分享链接
        return "http://localhost:8080/share/" + documentId + "?token=" + token;
    }

    @Override
    public IPage<DocumentVO> queryMyUploadedDocuments(DocumentQueryDTO queryDTO, Long userId) {
        if (userId == null) {
            return new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        }

        Page<Document> page = new Page<>((long) queryDTO.getPageNum(), (long) queryDTO.getPageSize());

        LambdaQueryWrapper<Document> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(queryDTO.getTitle())) {
            wrapper.like(Document::getTitle, queryDTO.getTitle());
        }

        if (StringUtils.hasText(queryDTO.getFileType())) {
            wrapper.eq(Document::getFileType, queryDTO.getFileType());
        }

        if (StringUtils.hasText(queryDTO.getKeyword())) {
            wrapper.and(w -> w
                    .like(Document::getTitle, queryDTO.getKeyword())
                    .or()
                    .like(Document::getDescription, queryDTO.getKeyword())
                    .or()
                    .like(Document::getParsedContent, queryDTO.getKeyword()));
        }

        wrapper.eq(Document::getUploadUserId, userId);
        wrapper.eq(Document::getIsDeleted, 0);

        if (StringUtils.hasText(queryDTO.getSortBy())) {
            boolean isAsc = "asc".equalsIgnoreCase(queryDTO.getSortOrder());
            switch (queryDTO.getSortBy()) {
                case "createTime":
                    wrapper.orderBy(true, isAsc, Document::getCreateTime);
                    break;
                case "viewCount":
                    wrapper.orderBy(true, isAsc, Document::getViewCount);
                    break;
                case "downloadCount":
                    wrapper.orderBy(true, isAsc, Document::getDownloadCount);
                    break;
                default:
                    wrapper.orderByDesc(Document::getCreateTime);
            }
        } else {
            wrapper.orderByDesc(Document::getCreateTime);
        }

        IPage<Document> documentPage = this.page(page, wrapper);

        return documentPage.convert(this::convertToVO);
    }

    @Override
    public IPage<DocumentVO> queryMyFavoriteDocuments(DocumentQueryDTO queryDTO, Long userId) {
        if (userId == null) {
            Page<Document> emptyPage = new Page<>((long) queryDTO.getPageNum(), (long) queryDTO.getPageSize());
            return emptyPage.convert(this::convertToVO);
        }

        // 首先获取用户收藏的文档ID列表
        List<Long> favoriteDocumentIds = userBehaviorService.getDocumentIdsByUserAndType(userId, "FAVORITE");

        if (favoriteDocumentIds.isEmpty()) {
            Page<Document> emptyPage = new Page<>((long) queryDTO.getPageNum(), (long) queryDTO.getPageSize());
            return emptyPage.convert(this::convertToVO);
        }

        Page<Document> page = new Page<>((long) queryDTO.getPageNum(), (long) queryDTO.getPageSize());

        LambdaQueryWrapper<Document> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(queryDTO.getTitle())) {
            wrapper.like(Document::getTitle, queryDTO.getTitle());
        }

        if (StringUtils.hasText(queryDTO.getFileType())) {
            wrapper.eq(Document::getFileType, queryDTO.getFileType());
        }

        if (StringUtils.hasText(queryDTO.getKeyword())) {
            wrapper.and(w -> w
                    .like(Document::getTitle, queryDTO.getKeyword())
                    .or()
                    .like(Document::getDescription, queryDTO.getKeyword())
                    .or()
                    .like(Document::getParsedContent, queryDTO.getKeyword()));
        }

        wrapper.in(Document::getId, favoriteDocumentIds);
        wrapper.eq(Document::getIsDeleted, 0);

        if (StringUtils.hasText(queryDTO.getSortBy())) {
            boolean isAsc = "asc".equalsIgnoreCase(queryDTO.getSortOrder());
            switch (queryDTO.getSortBy()) {
                case "createTime":
                    wrapper.orderBy(true, isAsc, Document::getCreateTime);
                    break;
                case "viewCount":
                    wrapper.orderBy(true, isAsc, Document::getViewCount);
                    break;
                case "downloadCount":
                    wrapper.orderBy(true, isAsc, Document::getDownloadCount);
                    break;
                default:
                    wrapper.orderByDesc(Document::getCreateTime);
            }
        } else {
            wrapper.orderByDesc(Document::getCreateTime);
        }

        IPage<Document> documentPage = this.page(page, wrapper);

        return documentPage.convert(this::convertToVO);
    }

    @Override
    public IPage<DocumentVO> queryMyHistoryDocuments(DocumentQueryDTO queryDTO, Long userId) {
        if (userId == null) {
            Page<Document> emptyPage = new Page<>((long) queryDTO.getPageNum(), (long) queryDTO.getPageSize());
            return emptyPage.convert(this::convertToVO);
        }

        // 首先获取用户浏览过的文档ID列表
        List<Long> historyDocumentIds = userBehaviorService.getDocumentIdsByUserAndType(userId, "VIEW");

        if (historyDocumentIds.isEmpty()) {
            Page<Document> emptyPage = new Page<>((long) queryDTO.getPageNum(), (long) queryDTO.getPageSize());
            return emptyPage.convert(this::convertToVO);
        }

        Page<Document> page = new Page<>((long) queryDTO.getPageNum(), (long) queryDTO.getPageSize());

        LambdaQueryWrapper<Document> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(queryDTO.getTitle())) {
            wrapper.like(Document::getTitle, queryDTO.getTitle());
        }

        if (StringUtils.hasText(queryDTO.getFileType())) {
            wrapper.eq(Document::getFileType, queryDTO.getFileType());
        }

        if (StringUtils.hasText(queryDTO.getKeyword())) {
            wrapper.and(w -> w
                    .like(Document::getTitle, queryDTO.getKeyword())
                    .or()
                    .like(Document::getDescription, queryDTO.getKeyword())
                    .or()
                    .like(Document::getParsedContent, queryDTO.getKeyword()));
        }

        wrapper.in(Document::getId, historyDocumentIds);
        wrapper.eq(Document::getIsDeleted, 0);

        if (StringUtils.hasText(queryDTO.getSortBy())) {
            boolean isAsc = "asc".equalsIgnoreCase(queryDTO.getSortOrder());
            switch (queryDTO.getSortBy()) {
                case "createTime":
                    wrapper.orderBy(true, isAsc, Document::getCreateTime);
                    break;
                case "viewCount":
                    wrapper.orderBy(true, isAsc, Document::getViewCount);
                    break;
                case "downloadCount":
                    wrapper.orderBy(true, isAsc, Document::getDownloadCount);
                    break;
                default:
                    wrapper.orderByDesc(Document::getCreateTime);
            }
        } else {
            wrapper.orderByDesc(Document::getCreateTime);
        }

        IPage<Document> documentPage = this.page(page, wrapper);

        return documentPage.convert(this::convertToVO);
    }

    @Override
    public void batchRemoveFavorite(Long userId, java.util.List<Long> documentIds) {
        if (userId != null) {
            userBehaviorService.batchRemoveByUserAndDocumentAndType(userId, documentIds, "FAVORITE");
            // 更新文档的收藏计数
            for (Long documentId : documentIds) {
                updateFavoriteCount(documentId);
            }
        }
    }

    @Override
    public void clearHistory(Long userId) {
        if (userId != null) {
            userBehaviorService.removeAllByUserAndType(userId, "VIEW");
        }
    }

    @Override
    public void batchRemoveHistory(Long userId, java.util.List<Long> documentIds) {
        if (userId != null) {
            userBehaviorService.batchRemoveByUserAndDocumentAndType(userId, documentIds, "VIEW");
        }
    }

    @Override
    public java.util.Map<String, Object> getDocumentKnowledgeGraph(Long documentId) {
        // 调用KgService获取知识图谱数据
        return kgService.getDocumentKnowledgeGraph(documentId);
    }

    @Override
    public void rebuildDocumentKnowledgeGraph(Long documentId) {
        // 调用KgService重新构建知识图谱
        kgService.buildDocumentKnowledgeGraph(documentId);
    }

    @Override
    public List<Document> getDocumentsByIds(List<Long> documentIds) {
        if (documentIds == null || documentIds.isEmpty()) {
            return new java.util.ArrayList<>();
        }
        return this.list(new LambdaQueryWrapper<Document>()
                .in(Document::getId, documentIds)
                .eq(Document::getIsDeleted, 0));
    }

    @Override
    public List<Document> getOriginalDocuments(int limit) {
        return this.list(new LambdaQueryWrapper<Document>()
                .eq(Document::getIsDeleted, 0)
                .isNotNull(Document::getOverallSimilarity)
                .isNotNull(Document::getAiProbability)
                .last("ORDER BY (1 - overall_similarity) * 0.6 + (1 - ai_probability) * 0.4 DESC LIMIT " + limit));
    }

    @Override
    public List<Long> getHotDocumentIds(int limit) {
        List<Document> hotDocuments = this.list(new LambdaQueryWrapper<Document>()
                .eq(Document::getIsDeleted, 0)
                .orderByDesc(Document::getViewCount)
                .last("LIMIT " + limit));

        List<Long> hotDocumentIds = new java.util.ArrayList<>();
        for (Document document : hotDocuments) {
            hotDocumentIds.add(document.getId());
        }
        return hotDocumentIds;
    }

    @Override
    public List<Long> getRandomHotDocumentIds(int limit, int offset) {
        int totalCount = Math.toIntExact(this.count(new LambdaQueryWrapper<Document>()
                .eq(Document::getIsDeleted, 0)));

        if (totalCount <= limit) {
            return getHotDocumentIds(limit);
        }

        int maxOffset = Math.min(offset, totalCount - limit);
        int actualOffset = (int) (Math.random() * (maxOffset + 1));

        List<Document> documents = this.list(new LambdaQueryWrapper<Document>()
                .eq(Document::getIsDeleted, 0)
                .orderByDesc(Document::getViewCount)
                .last("LIMIT " + actualOffset + ", " + limit));

        List<Long> documentIds = new java.util.ArrayList<>();
        for (Document document : documents) {
            documentIds.add(document.getId());
        }
        return documentIds;
    }

    @Override
    public IPage<DocumentVO> queryAdminDocumentPage(DocumentQueryDTO queryDTO) {
        Page<Document> page = new Page<>(
                queryDTO.getPageNum(), queryDTO.getPageSize());

        LambdaQueryWrapper<Document> wrapper = new LambdaQueryWrapper<>();

        // 添加查询条件
        if (org.springframework.util.StringUtils.hasText(queryDTO.getKeyword())) {
            wrapper.and(w -> w
                    .like(Document::getTitle, queryDTO.getKeyword())
                    .or()
                    .like(Document::getDescription, queryDTO.getKeyword()));
        }

        if (org.springframework.util.StringUtils.hasText(queryDTO.getFileType())) {
            wrapper.eq(Document::getFileType, queryDTO.getFileType());
        }

        if (org.springframework.util.StringUtils.hasText(queryDTO.getCategory())) {
            wrapper.eq(Document::getCategory, queryDTO.getCategory());
        }

        if (queryDTO.getTags() != null && !queryDTO.getTags().isEmpty()) {
            String[] tags = queryDTO.getTags().split(",");
            for (String tag : tags) {
                wrapper.inSql(Document::getId,
                        "SELECT document_id FROM doc_topic WHERE topic_type = 'KEYWORD' AND topic_value = '"
                                + tag.trim() + "'");
            }
        }

        wrapper.eq(Document::getIsDeleted, 0);

        // 排序
        if (org.springframework.util.StringUtils.hasText(queryDTO.getSortBy())) {
            boolean isAsc = "asc".equalsIgnoreCase(queryDTO.getSortOrder());
            switch (queryDTO.getSortBy()) {
                case "createTime":
                    wrapper.orderBy(true, isAsc, Document::getCreateTime);
                    break;
                case "viewCount":
                    wrapper.orderBy(true, isAsc, Document::getViewCount);
                    break;
                case "downloadCount":
                    wrapper.orderBy(true, isAsc, Document::getDownloadCount);
                    break;
                default:
                    wrapper.orderByDesc(Document::getCreateTime);
            }
        } else {
            wrapper.orderByDesc(Document::getCreateTime);
        }

        IPage<Document> documentPage = this.page(page, wrapper);

        return documentPage.convert(this::convertToVO);
    }

    @Override
    public void adminDeleteDocument(Long id) {
        Document document = this.getById(id);
        if (document == null) {
            throw new com.taoxier.smartdochub.common.exception.BusinessException("文档不存在");
        }
        document.setIsDeleted((byte) 1);
        this.updateById(document);
    }

    @Override
    public void batchDeleteDocuments(List<Long> ids) {
        this.updateBatchById(
                ids.stream().map(id -> {
                    Document doc = new Document();
                    doc.setId(id);
                    doc.setIsDeleted((byte) 1);
                    return doc;
                }).collect(java.util.stream.Collectors.toList()));
    }

    @Override
    public Object getAdminDocumentStats() {
        long totalDocuments = this
                .count(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Document>()
                        .eq(Document::getIsDeleted, 0));

        long totalViews = this.list(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Document>()
                .eq(Document::getIsDeleted, 0))
                .stream().mapToLong(Document::getViewCount).sum();

        long totalDownloads = this
                .list(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Document>()
                        .eq(Document::getIsDeleted, 0))
                .stream().mapToLong(Document::getDownloadCount).sum();

        long todayDocuments = this
                .count(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Document>()
                        .eq(Document::getIsDeleted, 0)
                        .ge(Document::getCreateTime, java.time.LocalDate.now().atStartOfDay()));

        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalDocuments", totalDocuments);
        stats.put("totalViews", totalViews);
        stats.put("totalDownloads", totalDownloads);
        stats.put("todayDocuments", todayDocuments);

        return stats;
    }

    @Override
    public List<Map<String, Object>> getUploadTrend() {
        List<Map<String, Object>> trend = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(23, 59, 59);

            long count = this.count(new LambdaQueryWrapper<Document>()
                    .eq(Document::getIsDeleted, 0)
                    .ge(Document::getCreateTime, startOfDay)
                    .le(Document::getCreateTime, endOfDay));

            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", date.format(DateTimeFormatter.ofPattern("MM/dd")));
            dayData.put("count", count);
            trend.add(dayData);
        }

        return trend;
    }

    @Override
    public List<Map<String, Object>> getDocumentTypeRatio() {
        List<Document> documents = this.list(new LambdaQueryWrapper<Document>()
                .eq(Document::getIsDeleted, 0));

        Map<String, Long> typeCount = new HashMap<>();
        for (Document doc : documents) {
            String fileType = doc.getFileType();
            if (fileType == null || fileType.isEmpty()) {
                fileType = "其他";
            } else {
                // 简化文件类型
                if (fileType.contains("pdf")) {
                    fileType = "PDF";
                } else if (fileType.contains("word") || fileType.contains("docx") || fileType.contains("doc")) {
                    fileType = "Word";
                } else if (fileType.contains("excel") || fileType.contains("xlsx") || fileType.contains("xls")) {
                    fileType = "Excel";
                } else if (fileType.contains("powerpoint") || fileType.contains("pptx") || fileType.contains("ppt")) {
                    fileType = "PowerPoint";
                } else {
                    fileType = "其他";
                }
            }
            typeCount.put(fileType, typeCount.getOrDefault(fileType, 0L) + 1);
        }

        List<Map<String, Object>> ratio = new ArrayList<>();
        for (Map.Entry<String, Long> entry : typeCount.entrySet()) {
            Map<String, Object> typeData = new HashMap<>();
            typeData.put("name", entry.getKey());
            typeData.put("value", entry.getValue());
            ratio.add(typeData);
        }

        return ratio;
    }
}
