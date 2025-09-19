package com.resume.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("theme_config")
public class ThemeConfig {
    @TableId(type = IdType.ASSIGN_ID)
    private Integer id;
    private String name;
    private String primaryColor;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}