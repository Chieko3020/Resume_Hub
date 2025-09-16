package com.resume.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.resume.entity.UserFile;
import com.resume.mapper.UserFileMapper;
import com.resume.service.UserFileService;
import com.resume.service.LocalOssService;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.List;
import java.util.UUID;

/**
 * 用户文件服务实现类
 */
@Service
public class UserFileServiceImpl implements UserFileService {
    
    @Autowired
    private UserFileMapper userFileMapper;
    
    @Autowired
    private LocalOssService localOssService;
    
    @Value("${file.upload.path:./uploads}")
    private String uploadPath;
    
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final String[] ALLOWED_EXTENSIONS = {".docx", ".pdf"};
    
    @Override
    public UserFile uploadFile(Integer userId, MultipartFile file) {
        // 验证文件
        validateFile(file);
        
        try {
            // 创建上传目录
            createUploadDirectory();
            
            // 生成文件名
            String originalName = file.getOriginalFilename();
            String extension = getFileExtension(originalName);
            String fileName = generateFileName(userId, originalName);
            
            // 保存文件
            Path filePath = Paths.get(uploadPath, fileName);
            Files.copy(file.getInputStream(), filePath);
            
            // 删除用户之前的文件
            deleteUserFile(userId);
            
            // 保存文件信息到数据库
            UserFile userFile = new UserFile();
            userFile.setUserId(userId);
            userFile.setOriginalName(originalName);
            userFile.setFileName(fileName);
            userFile.setFilePath(filePath.toString());
            userFile.setFileSize(file.getSize());
            userFile.setFileType(extension);
            userFile.setUploadTime(LocalDateTime.now());
            userFile.setCreateTime(LocalDateTime.now());
            userFile.setUpdateTime(LocalDateTime.now());
            
            userFileMapper.insert(userFile);
            return userFile;
            
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }
    
    @Override
    public UserFile getUserFile(Integer userId) {
        QueryWrapper<UserFile> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        return userFileMapper.selectOne(queryWrapper);
    }
    
    @Override
    public boolean deleteUserFile(Integer userId) {
        UserFile existingFile = getUserFile(userId);
        if (existingFile != null) {
            // 使用LocalOssService删除物理文件
            try {
                localOssService.deleteFile(existingFile.getFilePath());
            } catch (Exception e) {
                System.err.println("删除物理文件失败: " + e.getMessage());
            }
            
            // 删除数据库记录
            QueryWrapper<UserFile> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId);
            return userFileMapper.delete(queryWrapper) > 0;
        }
        return true;
    }
    
    @Override
    public List<UserFile> getAllFiles() {
        return userFileMapper.selectList(null);
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
    private String generateFileName(Integer userId, String originalName) {
        String extension = getFileExtension(originalName);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return userId + "_" + timestamp + "_" + uuid + extension;
    }
    
    /**
     * 创建上传目录
     */
    private void createUploadDirectory() throws IOException {
        Path path = Paths.get(uploadPath);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
    }
}
