package com.resume.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.resume.entity.AdminUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 管理员Mapper接口
 */
@Mapper
public interface AdminUserMapper extends BaseMapper<AdminUser> {
} 