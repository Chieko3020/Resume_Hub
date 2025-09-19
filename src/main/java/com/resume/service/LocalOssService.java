package com.resume.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * 本地OSS存储服务接口
 */
public interface LocalOssService {
    
    /**
     * 上传文件到本地OSS
     * @param file 上传的文件
     * @param folder 存储文件夹
     * @return 文件访问URL
     */
    String uploadFile(MultipartFile file, String folder);
    
    /**
     * 删除文件
     * @param fileUrl 文件URL
     * @return 删除结果
     */
    boolean deleteFile(String fileUrl);
    
    /**
     * 获取文件路径
     * @param fileUrl 文件URL
     * @return 文件路径
     */
    Path getFilePath(String fileUrl);
    
    /**
     * 检查文件是否存在
     * @param fileUrl 文件URL
     * @return 是否存在
     */
    boolean fileExists(String fileUrl);
}
