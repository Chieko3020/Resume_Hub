package com.resume.service.impl;

import com.resume.service.LocalOssService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 本地OSS存储服务实现
 */
@Service
public class LocalOssServiceImpl implements LocalOssService {
    
    @Value("${local.oss.root-path:./oss-storage}")
    private String rootPath;
    
    @Value("${local.oss.base-url:http://localhost:8080/oss}")
    private String baseUrl;
    
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final String[] ALLOWED_EXTENSIONS = {".docx", ".pdf"};
    
    @Override
    public String uploadFile(MultipartFile file, String folder) {
        // 验证文件
        validateFile(file);
        
        try {
            // 创建目录结构
            String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            Path targetDir = Paths.get(rootPath, folder, datePath);
            Files.createDirectories(targetDir);
            
            // 生成文件名
            String originalName = file.getOriginalFilename();
            String extension = getFileExtension(originalName);
            String fileName = generateFileName(originalName);
            
            // 保存文件
            Path filePath = targetDir.resolve(fileName);
            Files.copy(file.getInputStream(), filePath);
            
            // 返回访问URL
            return baseUrl + "/" + folder + "/" + datePath + "/" + fileName;
            
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }
    
    @Override
    public boolean deleteFile(String fileUrl) {
        try {
            Path filePath = getFilePath(fileUrl);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                return true;
            }
            return false;
        } catch (IOException e) {
            System.err.println("删除文件失败: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public Path getFilePath(String fileUrl) {
        // 从URL中提取相对路径
        String relativePath = fileUrl.replace(baseUrl + "/", "");
        // 处理Windows路径中的冒号问题
        relativePath = relativePath.replace(":", "");
        return Paths.get(rootPath, relativePath);
    }
    
    @Override
    public boolean fileExists(String fileUrl) {
        Path filePath = getFilePath(fileUrl);
        return Files.exists(filePath);
    }
    
    /**
     * 验证文件
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("文件大小不能超过5MB");
        }
        
        String originalName = file.getOriginalFilename();
        if (originalName == null) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        
        String extension = getFileExtension(originalName);
        boolean allowed = false;
        for (String allowedExt : ALLOWED_EXTENSIONS) {
            if (allowedExt.equalsIgnoreCase(extension)) {
                allowed = true;
                break;
            }
        }
        
        if (!allowed) {
            throw new IllegalArgumentException("只支持docx和pdf格式的文件");
        }
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return fileName.substring(lastDotIndex);
    }
    
    /**
     * 生成文件名
     */
    private String generateFileName(String originalName) {
        String extension = getFileExtension(originalName);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return timestamp + "_" + uuid + extension;
    }
}
