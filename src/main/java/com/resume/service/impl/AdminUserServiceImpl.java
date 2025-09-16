package com.resume.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.resume.entity.AdminUser;
import com.resume.entity.User;
import com.resume.mapper.AdminUserMapper;
import com.resume.service.AdminUserService;
import com.resume.service.UserService;
import com.resume.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 管理员服务实现类
 */
@Service
public class AdminUserServiceImpl extends com.baomidou.mybatisplus.extension.service.impl.ServiceImpl<com.resume.mapper.AdminUserMapper, com.resume.entity.AdminUser> implements AdminUserService {
    
    @Autowired
    private AdminUserMapper adminUserMapper;
    
    @Autowired
    private UserService userService;
    
    @Override
    public AdminUser login(String username, String password) {
        // 从 user 表中查找管理员用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username).eq("role", "admin");
        User user = userService.getOne(queryWrapper);
        
        if (user != null && PasswordUtil.matches(password, user.getPassword())) {
            // 转换为 AdminUser 对象
            AdminUser adminUser = new AdminUser();
            adminUser.setId(user.getId());
            adminUser.setUsername(user.getUsername());
            adminUser.setPassword(user.getPassword());
            adminUser.setRole(user.getRole());
            adminUser.setCreateTime(user.getCreateTime());
            adminUser.setUpdateTime(user.getUpdateTime());
            return adminUser;
        }
        return null;
    }
    
    @Override
    public AdminUser findByUsername(String username) {
        // 从 user 表中查找管理员用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username).eq("role", "admin");
        User user = userService.getOne(queryWrapper);
        
        if (user != null) {
            // 转换为 AdminUser 对象
            AdminUser adminUser = new AdminUser();
            adminUser.setId(user.getId());
            adminUser.setUsername(user.getUsername());
            adminUser.setPassword(user.getPassword());
            adminUser.setRole(user.getRole());
            adminUser.setCreateTime(user.getCreateTime());
            adminUser.setUpdateTime(user.getUpdateTime());
            return adminUser;
        }
        return null;
    }
    
    @Override
    public boolean changePassword(String username, String oldPassword, String newPassword) {
        // 从 user 表中查找管理员用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username).eq("role", "admin");
        User user = userService.getOne(queryWrapper);
        
        if (user != null && PasswordUtil.matches(oldPassword, user.getPassword())) {
            user.setPassword(PasswordUtil.encode(newPassword));
            return userService.updateById(user);
        }
        return false;
    }
} 