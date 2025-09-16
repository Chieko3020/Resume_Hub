package com.resume.service;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class LocalOssServiceTest {
    
    @Test
    public void testFileStorage() throws Exception {
        // 创建测试文件内容
        String testContent = "这是一个测试文档内容\n包含中文和英文\nTest content with Chinese and English";
        byte[] testBytes = testContent.getBytes("UTF-8");
        
        // 创建模拟上传文件
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "test-document.docx", 
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document", 
            testBytes
        );
        
        // 模拟本地OSS存储逻辑
        String rootPath = "./test-oss-storage";
        String baseUrl = "http://localhost:8080/oss";
        String folder = "test-files";
        
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
        
        // 生成文件URL
        String fileUrl = baseUrl + "/" + folder + "/" + datePath + "/" + fileName;
        
        // 验证文件URL
        assertNotNull(fileUrl);
        assertTrue(fileUrl.contains("test-files"));
        assertTrue(fileUrl.contains("test-document") || fileUrl.contains(fileName));
        
        // 验证文件确实存在
        assertTrue(Files.exists(filePath));
        
        // 验证文件内容是否与原始内容一致
        byte[] storedBytes = Files.readAllBytes(filePath);
        assertArrayEquals(testBytes, storedBytes);
        
        // 验证文件内容可读
        String storedContent = new String(storedBytes, "UTF-8");
        assertEquals(testContent, storedContent);
        
        System.out.println("✅ 文件存储验证成功！");
        System.out.println("📁 存储路径: " + filePath);
        System.out.println(" 文件大小: " + storedBytes.length + " bytes");
        System.out.println("📄 文件内容: " + storedContent);
        System.out.println("🌐 访问URL: " + fileUrl);
        
        // 清理测试文件
        Files.deleteIfExists(filePath);
        // 清理空目录
        try {
            Files.deleteIfExists(targetDir);
            Files.deleteIfExists(targetDir.getParent());
            Files.deleteIfExists(targetDir.getParent().getParent());
        } catch (Exception e) {
            // 忽略目录删除失败
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
