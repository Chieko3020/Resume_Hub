package com.resume.controller;

import com.resume.service.LocalOssService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class OssControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private LocalOssService localOssService;
    
    @Test
    public void testFileAccess() throws Exception {
        // 创建测试文件
        String testContent = "这是一个测试PDF文件内容\nTest PDF content";
        Path testFile = Paths.get("./test-file.pdf");
        Files.write(testFile, testContent.getBytes("UTF-8"));
        
        // 模拟服务返回
        when(localOssService.fileExists(anyString())).thenReturn(true);
        when(localOssService.getFilePath(anyString())).thenReturn(testFile);
        
        // 测试文件访问
        mockMvc.perform(get("/oss/test-files/2025/09/17/test-file.pdf?path=test-files/2025/09/17/test-file.pdf"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/pdf"))
                .andExpect(content().bytes(testContent.getBytes("UTF-8")));
        
        // 清理测试文件
        Files.deleteIfExists(testFile);
        
        System.out.println("✅ 文件访问验证成功！");
        System.out.println("📄 文件内容: " + testContent);
    }
}
