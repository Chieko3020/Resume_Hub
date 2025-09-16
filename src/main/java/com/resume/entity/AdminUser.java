package com.resume.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理员实体类
 */
@Data
@TableName("admin_user")
public class AdminUser {
    @TableId(type = IdType.ASSIGN_ID)
    private Integer id;
    /** 登录名 */
    private String username;
    /** 密码(BCrypt加密) */
    private String password;
    /** 角色（admin/user） */
    private String role;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;
}