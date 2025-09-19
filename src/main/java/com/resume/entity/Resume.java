package com.resume.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 简历实体类
 */
@Data
@TableName("resume")
public class Resume {
    @TableId(type = IdType.AUTO)
    private Integer id;
    /** 所属用户ID */
    private Integer userId;
    /** 简历名称 */
    private String name;
    /** 简历内容(JSON格式) */
    private String content;
    /** 主题ID */
    private Integer themeId;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;
}