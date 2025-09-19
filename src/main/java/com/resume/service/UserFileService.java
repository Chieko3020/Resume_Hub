package com.resume.service;

import com.resume.entity.UserFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 用户文件服务接口
 */
public interface UserFileService {
    
    /**
     * 上传文件
     * @param userId 用户ID
     * @param file 上传的文件
     * @return 上传结果
     */
    UserFile uploadFile(Integer userId, MultipartFile file);
    
    /**
     * 获取用户的文件
     * @param userId 用户ID
     * @return 用户文件
     */
    UserFile getUserFile(Integer userId);
    
    /**
     * 删除用户文件
     * @param userId 用户ID
     * @return 删除结果
     */
    boolean deleteUserFile(Integer userId);
    
    /**
     * 获取所有用户文件
     * @return 文件列表
     */
    List<UserFile> getAllFiles();
}
