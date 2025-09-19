package com.resume.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.resume.entity.UserFile;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户文件Mapper接口
 */
@Mapper
public interface UserFileMapper extends BaseMapper<UserFile> {
}
