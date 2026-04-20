package com.taoxier.smartdochub.file.service;

import com.taoxier.smartdochub.file.model.FileInfo;
import org.springframework.web.multipart.MultipartFile;

/**
 * 对象存储服务接口层
 */
public interface FileService {

    /**
     * 上传文件
     * 
     * @param file 表单文件对象
     * @return 文件信息
     */
    FileInfo uploadFile(MultipartFile file);

    /**
     * 删除文件
     *
     * @param filePath 文件完整URL
     * @return 删除结果
     */
    boolean deleteFile(String filePath);

    /**
     * 获取文档预览URL
     *
     * @param filePath 文件完整URL
     * @return 预览URL
     */
    String getPreviewUrl(String filePath);

    /**
     * 获取COS存储桶名称
     *
     * @return 存储桶名称
     */
    String getBucketName();

    /**
     * 获取COS地域
     *
     * @return 地域
     */
    String getRegion();

    /**
     * 下载文件
     *
     * @param filePath     文件完整URL
     * @param outputStream 输出流
     */
    void downloadFile(String filePath, java.io.OutputStream outputStream);

}
