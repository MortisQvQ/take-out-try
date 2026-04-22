package com.example.takeouttry.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class User {

    private Long id;                        // 主键ID
    private String username;                // 用户名
    private String password;                // 密码（加密存储）
    private String phone;                   // 手机号
    private Integer role;                   // 角色：1=普通用户，2=商家
    private LocalDateTime createTime;       // 创建时间
    private LocalDateTime updateTime;       // 更新时间
    private String avatarUrl;               // 对应数据库的 avatar_url 字段
}