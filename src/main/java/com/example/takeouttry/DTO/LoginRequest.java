package com.example.takeouttry.DTO;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
    private String type; // 新增：登录类型
}