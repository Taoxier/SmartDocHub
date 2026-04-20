package com.taoxier.smartdochub.file.service.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.taoxier.smartdochub.file.model.FileInfo;
import com.taoxier.smartdochub.file.service.FileService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.time.LocalDateTime;

/**
 * 本地存储服务类
 */
@Data
@Slf4j
@Component
@ConditionalOnProperty(value = "oss.type", havingValue = "local")
@ConfigurationProperties(prefix = "oss.local")
@RequiredArgsConstructor
public class LocalFileService implements FileService {

    @Value("${oss.local.storage-path}")
    private String storagePath;

    /**
     * 上传文件方法
     *
     * @param file 表单文件对象
     * @return 文件信息
     */
    @Override
    public FileInfo uploadFile(MultipartFile file) {
        // 获取文件名
        String originalFilename = file.getOriginalFilename();
        // 获取文件后缀
        String suffix = FileUtil.getSuffix(originalFilename);
        // 生成uuid
        String fileName = IdUtil.simpleUUID() + "." + suffix;
        ;
        // 生成文件名(日期文件夹)
        String folder = DateUtil.format(LocalDateTime.now(), DatePattern.PURE_DATE_PATTERN);
        String filePrefix = storagePath.endsWith(File.separator) ? storagePath : storagePath + File.separator;
        // try-with-resource 语法糖自动释放流
        try (InputStream inputStream = file.getInputStream()) {
            // 上传文件
            FileUtil.writeFromStream(inputStream, filePrefix + folder + File.separator + fileName);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败");
        }
        // 获取文件访问路径，因为这里是本地存储，所以直接返回文件的相对路径，需要前端自行处理访问前缀
        String fileUrl = File.separator + folder + File.separator + fileName;
        FileInfo fileInfo = new FileInfo();
        fileInfo.setName(originalFilename);
        fileInfo.setUrl(fileUrl);
        return fileInfo;
    }

    /**
     * 删除文件
     * 
     * @param filePath 文件完整URL
     * @return 是否删除成功
     */
    @Override
    public boolean deleteFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return false;
        }
        if (FileUtil.isDirectory(storagePath + filePath)) {
            return false;
        }
        return FileUtil.del(storagePath + filePath);
    }

    @Override
    public String getPreviewUrl(String filePath) {
        return null;
    }

    @Override
    public String getBucketName() {
        return "local";
    }

    @Override
    public String getRegion() {
        return null;
    }

    @Override
    public void downloadFile(String filePath, java.io.OutputStream outputStream) {
        try {
            String filePrefix = storagePath.endsWith(File.separator) ? storagePath : storagePath + File.separator;
            String fullPath = filePrefix + filePath;

            // 从本地文件系统读取文件
            File file = new File(fullPath);
            if (!file.exists() || !file.isFile()) {
                throw new RuntimeException("文件不存在");
            }

            try (java.io.InputStream inputStream = new java.io.FileInputStream(file)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.flush();
            }
        } catch (Exception e) {
            log.error("本地文件下载失败", e);
            throw new RuntimeException("文件下载失败", e);
        }
    }
}
