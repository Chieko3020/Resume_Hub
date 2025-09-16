package com.resume.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {
    @TableId(type = com.baomidou.mybatisplus.annotation.IdType.AUTO)
    private Integer id;
    private String username;
    private String password;
    private String role; // admin/user
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
