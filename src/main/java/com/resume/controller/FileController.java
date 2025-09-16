package com.resume.controller;

import com.resume.entity.UserFile;
import com.resume.service.UserFileService;
import com.resume.service.LocalOssService;
import com.resume.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 文件上传下载控制器
 */
@RestController
@RequestMapping("/api/user/file")
public class FileController {
    
    @Autowired
    private UserFileService userFileService;
    
    @Autowired
    private LocalOssService localOssService;
    
    @Autowired
    private com.resume.util.JwtUtil jwtUtil;
    
    @Autowired
    private com.resume.service.UserService userService;
    
    @Autowired
    private com.resume.mapper.UserFileMapper userFileMapper;
    
    /**
     * 上传文件
     */
    @PostMapping("/upload")
    public Result<?> uploadFile(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        try {
            Integer userId = getUserIdFromToken(request);
            
            // 验证文件
            if (file.isEmpty()) {
                return Result.error("上传文件不能为空");
            }
            
            String originalFilename = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFilename);
            
            // 检查文件类型
            if (!fileExtension.equalsIgnoreCase(".pdf") && !fileExtension.equalsIgnoreCase(".docx")) {
                return Result.error("只支持PDF和DOCX格式的文件");
            }
            
            // 检查文件大小
            if (file.getSize() > 5 * 1024 * 1024) {
                return Result.error("文件大小不能超过5MB");
            }
            
            // 删除用户之前的文件
            userFileService.deleteUserFile(userId);
            
            // 使用本地OSS上传文件
            String fileUrl = localOssService.uploadFile(file, "user-files");
            
            // 保存文件信息到数据库
            UserFile userFile = new UserFile();
            userFile.setUserId(userId);
            userFile.setOriginalName(originalFilename);
            userFile.setFileName(originalFilename);
            userFile.setFilePath(fileUrl);
            userFile.setFileSize(file.getSize());
            userFile.setFileType(fileExtension);
            userFile.setUploadTime(java.time.LocalDateTime.now());
            userFile.setCreateTime(java.time.LocalDateTime.now());
            userFile.setUpdateTime(java.time.LocalDateTime.now());
            
            // 保存到数据库
            userFileMapper.insert(userFile);
            
            return Result.success("文件上传成功", userFile);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户文件信息
     */
    @GetMapping("/info")
    public Result<UserFile> getUserFileInfo(HttpServletRequest request) {
        try {
            Integer userId = getUserIdFromToken(request);
            UserFile userFile = userFileService.getUserFile(userId);
            if (userFile != null) {
                return Result.success(userFile);
            } else {
                return Result.error("用户未上传文件");
            }
        } catch (Exception e) {
            return Result.error("获取文件信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 下载文件
     */
    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFile(HttpServletRequest request) {
        try {
            Integer userId = getUserIdFromToken(request);
            UserFile userFile = userFileService.getUserFile(userId);
            
            if (userFile == null) {
                return ResponseEntity.notFound().build();
            }
            
            // 使用本地OSS获取文件路径
            Path filePath = localOssService.getFilePath(userFile.getFilePath());
            File file = filePath.toFile();
            
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }
            
            Resource resource = new FileSystemResource(file);
            
            // 根据文件扩展名设置Content-Type
            String contentType = getContentTypeForFile(userFile.getOriginalName());
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + userFile.getOriginalName() + "\"")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);
                    
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * 删除文件
     */
    @DeleteMapping("/delete")
    public Result<?> deleteFile(HttpServletRequest request) {
        try {
            Integer userId = getUserIdFromToken(request);
            UserFile userFile = userFileService.getUserFile(userId);
            
            if (userFile == null) {
                return Result.error("未找到用户文件");
            }
            
            // 使用本地OSS删除文件
            boolean fileDeleted = localOssService.deleteFile(userFile.getFilePath());
            
            // 从数据库删除记录
            boolean dbDeleted = userFileService.deleteUserFile(userId);
            
            if (fileDeleted && dbDeleted) {
                return Result.success("文件删除成功");
            } else {
                return Result.error("文件删除失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("文件删除失败: " + e.getMessage());
        }
    }
    
    /**
     * 从Token中获取用户ID
     */
    private Integer getUserIdFromToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            com.resume.entity.User user = userService.lambdaQuery()
                    .eq(com.resume.entity.User::getUsername, username)
                    .one();
            if (user != null) {
                return user.getId();
            }
        }
        throw new RuntimeException("未登录或登录已过期");
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
     * 根据文件名获取Content-Type
     */
    private String getContentTypeForFile(String fileName) {
        String extension = getFileExtension(fileName).toLowerCase();
        switch (extension) {
            case ".pdf":
                return "application/pdf";
            case ".docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            default:
                return "application/octet-stream";
        }
    }
}
