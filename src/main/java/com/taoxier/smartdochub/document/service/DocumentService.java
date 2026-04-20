package com.taoxier.smartdochub.document.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.taoxier.smartdochub.document.model.dto.DocumentQueryDTO;
import com.taoxier.smartdochub.document.model.dto.DocumentUpdateDTO;
import com.taoxier.smartdochub.document.model.entity.Document;
import com.taoxier.smartdochub.document.model.entity.DocumentVersion;
import com.baomidou.mybatisplus.extension.service.IService;
import com.taoxier.smartdochub.document.model.vo.DocumentDetailVO;
import com.taoxier.smartdochub.document.model.vo.DocumentVO;
import java.util.List;
import java.util.Map;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;

public interface DocumentService extends IService<Document> {

    @Transactional
    Document uploadAndParse(MultipartFile file, Long userId);

    @Transactional
    Document uploadNewVersion(MultipartFile file, Long documentId, Long userId);

    IPage<DocumentVO> queryDocumentPage(DocumentQueryDTO queryDTO, Long currentUserId);

    DocumentDetailVO getDocumentDetail(Long id, Long currentUserId);

    DocumentDetailVO getDocumentVersionDetail(Long id, Integer versionNumber, Long currentUserId);

    List<DocumentVersion> getDocumentVersions(Long documentId);

    void deleteDocument(Long id, Long currentUserId);

    void downloadDocument(Long id, Long currentUserId, HttpServletResponse response);

    void downloadDocumentVersion(Long id, Integer versionNumber, Long currentUserId, HttpServletResponse response);

    Document updateDocumentMetadata(DocumentUpdateDTO updateDTO, Long currentUserId);

    void incrementViewCount(Long id);

    void incrementDownloadCount(Long id);

    Document checkDuplicateByHash(String fileHash, Long userId);

    /**
     * 获取文档统计信息
     * 
     * @return 文档统计VO
     */
    com.taoxier.smartdochub.document.model.vo.DocumentStatsVO getDocumentStats();

    void updateFavoriteCount(Long documentId);

    void removeFavorite(Long userId, Long documentId);

    String generateShareUrl(Long documentId);

    /**
     * 获取我的上传文档
     * 
     * @param queryDTO 查询条件
     * @param userId   用户ID
     * @return 文档列表
     */
    IPage<DocumentVO> queryMyUploadedDocuments(DocumentQueryDTO queryDTO, Long userId);

    /**
     * 获取我的收藏文档
     * 
     * @param queryDTO 查询条件
     * @param userId   用户ID
     * @return 文档列表
     */
    IPage<DocumentVO> queryMyFavoriteDocuments(DocumentQueryDTO queryDTO, Long userId);

    /**
     * 获取我的浏览历史
     * 
     * @param queryDTO 查询条件
     * @param userId   用户ID
     * @return 文档列表
     */
    IPage<DocumentVO> queryMyHistoryDocuments(DocumentQueryDTO queryDTO, Long userId);

    /**
     * 批量取消收藏
     * 
     * @param userId      用户ID
     * @param documentIds 文档ID列表
     */
    void batchRemoveFavorite(Long userId, java.util.List<Long> documentIds);

    /**
     * 清空浏览历史
     * 
     * @param userId 用户ID
     */
    void clearHistory(Long userId);

    /**
     * 批量删除浏览历史
     * 
     * @param userId      用户ID
     * @param documentIds 文档ID列表
     */
    void batchRemoveHistory(Long userId, java.util.List<Long> documentIds);

    /**
     * 获取文档知识图谱
     * 
     * @param documentId 文档ID
     * @return 知识图谱数据
     */
    java.util.Map<String, Object> getDocumentKnowledgeGraph(Long documentId);

    /**
     * 重新构建文档知识图谱
     * 
     * @param documentId 文档ID
     */
    void rebuildDocumentKnowledgeGraph(Long documentId);

    /**
     * 根据文档ID列表获取文档详情
     * 
     * @param documentIds 文档ID列表
     * @return 文档列表
     */
    List<Document> getDocumentsByIds(List<Long> documentIds);

    /**
     * 获取原创力荐文档
     * 按 (1 - overall_similarity) * 0.6 + (1 - ai_probability) * 0.4 降序取前N篇
     * 
     * @param limit 返回数量
     * @return 原创力荐文档列表
     */
    List<Document> getOriginalDocuments(int limit);

    /**
     * 获取热门文档ID列表
     * 按浏览量降序取前N篇
     *
     * @param limit 返回数量
     * @return 热门文档ID列表
     */
    List<Long> getHotDocumentIds(int limit);

    /**
     * 获取随机热门文档ID列表
     * 在热门文档中加入随机偏移，增加推荐多样性
     *
     * @param limit  返回数量
     * @param offset 随机偏移量
     * @return 热门文档ID列表
     */
    List<Long> getRandomHotDocumentIds(int limit, int offset);

    /**
     * 查询管理员文档列表
     * 
     * @param queryDTO 查询条件
     * @return 文档分页
     */
    IPage<DocumentVO> queryAdminDocumentPage(DocumentQueryDTO queryDTO);

    /**
     * 管理员删除文档
     * 
     * @param id 文档ID
     */
    void adminDeleteDocument(Long id);

    /**
     * 批量删除文档
     * 
     * @param ids 文档ID列表
     */
    void batchDeleteDocuments(List<Long> ids);

    /**
     * 获取管理员文档统计数据
     * 
     * @return 统计数据
     */
    Object getAdminDocumentStats();

    List<Map<String, Object>> getUploadTrend();

    List<Map<String, Object>> getDocumentTypeRatio();
}
