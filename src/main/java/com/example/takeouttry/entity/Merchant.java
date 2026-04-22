package com.example.takeouttry.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商家实体类
 * 对应数据库表：merchant
 */
@Data
public class Merchant {

    private Long id;                        // 商家ID（主键，自增）
    private Long userId;                    // 关联的用户ID（商家账号）
    private String name;                    // 商家名称
    private String address;                 // 商家地址
    private String phone;                   // 商家联系电话
    private String businessHours;           // 营业时间，例如 "09:00-22:00"
    private String logo;                    // 商家Logo图片URL
   //private String description;             // 商家简介
    private Integer status;                 // 营业状态（0=停业 1=营业中）
    private LocalDateTime createTime;       // 创建时间
    private LocalDateTime updateTime;       // 更新时间

}