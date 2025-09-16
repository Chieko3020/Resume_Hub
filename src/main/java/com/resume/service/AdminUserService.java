package com.resume.service;

import com.resume.entity.AdminUser;

/**
 * 管理员服务接口
 */
import com.baomidou.mybatisplus.extension.service.IService;
import com.resume.entity.AdminUser;

public interface AdminUserService extends IService<AdminUser> {
    
    /**
     * 登录验证
     */
    AdminUser login(String username, String password);
    
    /**
     * 根据用户名查找管理员
     */
    AdminUser findByUsername(String username);
    
    /**
     * 修改密码
     */
    boolean changePassword(String username, String oldPassword, String newPassword);
} 