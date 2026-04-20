package com.taoxier.smartdochub.file.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.IdUtil;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.region.Region;
import com.taoxier.smartdochub.file.model.FileInfo;
import com.taoxier.smartdochub.file.service.FileService;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;

/**
 * @Author taoxier
 * @Date 2025/10/15 下午5:11
 * @描述 腾讯云COS文件存储服务类
 */
@Component
@ConditionalOnProperty(value = "oss.type", havingValue = "tencent")
@ConfigurationProperties(prefix = "oss.tencent")
@RequiredArgsConstructor
@Data
@Slf4j
public class TencentCosFileService implements FileService {

    /**
     * 访问密钥ID
     */
    private String secretId;

    /**
     * 访问密钥密钥
     */
    private String secretKey;

    /**
     * 存储桶名称
     */
    private String bucketName;

    /**
     * 地域
     */
    private String region;

    /**
     * 自定义域名
     */
    private String customDomain;

    private COSClient cosClient;

    @PostConstruct
    public void init() {
        // 初始化COS客户端
        COSCredentials credentials = new BasicCOSCredentials(secretId, secretKey);
        ClientConfig clientConfig = new ClientConfig(new Region(region));
        cosClient = new COSClient(credentials, clientConfig);
    }

    @Override
    public FileInfo uploadFile(MultipartFile file) {
        try {
            // 获取文件信息
            String originalFilename = file.getOriginalFilename();
            String suffix = FileUtil.getSuffix(originalFilename);
            String dateFolder = DateUtil.format(LocalDateTime.now(), "yyyyMMdd");
            String fileName = IdUtil.simpleUUID() + "." + suffix;
            String key = dateFolder + "/" + fileName; // COS中的文件键

            // 上传文件
            try (InputStream inputStream = file.getInputStream()) {
                com.qcloud.cos.model.ObjectMetadata metadata = new com.qcloud.cos.model.ObjectMetadata();
                metadata.setContentType(file.getContentType());
                cosClient.putObject(bucketName, key, inputStream, metadata);
            }

            // 构建文件URL
            String fileUrl;
            if (customDomain != null && !customDomain.isEmpty()) {
                fileUrl = customDomain + "/" + key;
            } else {
                // 默认URL格式: https://bucketName.region.myqcloud.com/key
                fileUrl = "https://" + bucketName + "." + region + ".myqcloud.com/" + key;
            }

            // 封装返回结果
            FileInfo fileInfo = new FileInfo();
            fileInfo.setName(originalFilename);
            fileInfo.setUrl(fileUrl);
            return fileInfo;
        } catch (Exception e) {
            log.error("腾讯云COS文件上传失败", e);
            throw new RuntimeException("文件上传失败", e);
        }
    }

    @Override
    public boolean deleteFile(String filePath) {
        Assert.notBlank(filePath, "删除文件路径不能为空");
        try {
            String key;
            if (customDomain != null && !customDomain.isEmpty()) {
                // 从自定义域名中提取文件键
                if (!filePath.startsWith(customDomain)) {
                    throw new RuntimeException("文件路径格式不正确");
                }
                key = filePath.substring(customDomain.length() + 1);
            } else {
                // 从默认URL中提取文件键
                String host = "https://" + bucketName + "." + region + ".myqcloud.com/";
                if (!filePath.startsWith(host)) {
                    throw new RuntimeException("文件路径格式不正确");
                }
                key = filePath.substring(host.length());
            }
            cosClient.deleteObject(bucketName, key);
            return true;
        } catch (Exception e) {
            log.error("腾讯云COS文件删除失败", e);
            throw new RuntimeException("文件删除失败", e);
        }
    }

    @Override
    public String getPreviewUrl(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return null;
        }
        try {
            String suffix = FileUtil.getSuffix(filePath).toLowerCase();
            if (!isPreviewSupported(suffix)) {
                return null;
            }

            // 确保使用自定义域名
            String previewUrl = filePath;

            // 如果有自定义域名，替换默认域名
            if (customDomain != null && !customDomain.isEmpty()) {
                String defaultHost = "https://" + bucketName + "." + region + ".myqcloud.com/";
                if (previewUrl.startsWith(defaultHost)) {
                    previewUrl = previewUrl.replace(defaultHost, customDomain + "/");
                }
            }

            // 确保使用HTTP而不是HTTPS
            if (previewUrl.startsWith("https://")) {
                previewUrl = previewUrl.replace("https://", "http://");
            }

            // 添加文档预览参数
            if (previewUrl.contains("?")) {
                previewUrl += "&ci-process=doc-preview&dstType=html";
            } else {
                previewUrl += "?ci-process=doc-preview&dstType=html";
            }

            return previewUrl;
        } catch (Exception e) {
            log.error("获取文档预览URL失败", e);
            return null;
        }
    }

    private boolean isPreviewSupported(String suffix) {
        return "doc".equals(suffix) || "docx".equals(suffix) ||
                "xls".equals(suffix) || "xlsx".equals(suffix) ||
                "ppt".equals(suffix) || "pptx".equals(suffix) ||
                "pdf".equals(suffix) || "txt".equals(suffix);
    }

    @Override
    public String getBucketName() {
        return bucketName;
    }

    @Override
    public String getRegion() {
        return region;
    }

    @Override
    public void downloadFile(String filePath, java.io.OutputStream outputStream) {
        log.info("开始下载文件, filePath: {}", filePath);
        log.info("COS配置 - bucketName: {}, region: {}, customDomain: {}", bucketName, region, customDomain);
        try {
            String key;
            String targetBucket;

            // 优先使用自定义域名
            if (customDomain != null && !customDomain.isEmpty() && filePath.startsWith(customDomain)) {
                log.info("使用自定义域名模式, customDomain: {}", customDomain);
                key = filePath.substring(customDomain.length() + 1);
                targetBucket = this.bucketName;
                log.info("提取的key: {}, 使用配置的bucket: {}", key, targetBucket);
            } else {
                // 从COS默认URL中提取文件键和bucket名称
                // 格式: https://bucketName.region.myqcloud.com/key
                // 例如:
                // https://smartdochub-1361216443.ap-guangzhou.myqcloud.com/20260407/xxx.docx
                String regionSuffix = "." + region + ".myqcloud.com/";
                int regionIdx = filePath.indexOf(regionSuffix);
                if (regionIdx > 0) {
                    // 提取bucket名称 (从 https:// 到 .region.myqcloud.com 之前)
                    int bucketStartIdx = filePath.indexOf("://") + 3;
                    targetBucket = filePath.substring(bucketStartIdx, regionIdx);
                    log.info("提取的bucket: {}", targetBucket);
                    // 转换为小写（COS要求bucket名称必须是小写）
                    targetBucket = targetBucket.toLowerCase();
                    // 提取key
                    key = filePath.substring(regionIdx + regionSuffix.length() - 1); // -1 因为regionSuffix包含前缀的.
                    if (key.startsWith("/")) {
                        key = key.substring(1);
                    }
                    log.info("使用默认域名模式, bucket: {}, key: {}", targetBucket, key);
                } else {
                    log.error("无法从文件路径中提取bucket和key, filePath: {}, 期望的regionSuffix: {}", filePath, regionSuffix);
                    throw new RuntimeException("文件路径格式不正确");
                }
            }

            // 从COS下载文件
            log.info("开始从COS获取文件, bucket: {}, key: {}", targetBucket, key);
            com.qcloud.cos.model.GetObjectRequest getObjectRequest = new com.qcloud.cos.model.GetObjectRequest(
                    targetBucket, key);
            com.qcloud.cos.model.COSObject cosObject = cosClient.getObject(getObjectRequest);

            try (java.io.InputStream inputStream = cosObject.getObjectContent()) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.flush();
            }
            log.info("文件下载成功");
        } catch (Exception e) {
            log.error("腾讯云COS文件下载失败", e);
            throw new RuntimeException("文件下载失败: " + e.getMessage(), e);
        }
    }

}
