package com.resume.controller;

import com.resume.service.LocalOssService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.nio.file.Path;

/**
 * OSS文件访问控制器
 */
@RestController
@RequestMapping("/oss")
public class OssController {
    
    @Autowired
    private LocalOssService localOssService;
    
    /**
     * 访问OSS文件
     */
    @GetMapping("/**")
    public ResponseEntity<Resource> getFile(HttpServletRequest request) {
        try {
            // 从请求路径中提取文件路径
            String requestPath = request.getRequestURI();
            String filePath = requestPath.replace("/oss/", "");
            
            // 构建完整的文件URL
            String fileUrl = "http://localhost:8080/oss/" + filePath;
            
            // 检查文件是否存在
            if (!localOssService.fileExists(fileUrl)) {
                return ResponseEntity.notFound().build();
            }
            
            // 获取文件路径
            Path actualFilePath = localOssService.getFilePath(fileUrl);
            File file = actualFilePath.toFile();
            
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }
            
            Resource resource = new FileSystemResource(file);
            
            // 根据文件扩展名设置Content-Type
            String contentType = getContentType(file.getName());
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getName() + "\"")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);
                    
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * 根据文件名获取Content-Type
     */
    private String getContentType(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        switch (extension) {
            case "pdf":
                return "application/pdf";
            case "docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            default:
                return "application/octet-stream";
        }
    }
}
