package com.example.takeouttry.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 菜品表实体类
 * 菜品
 */
@Data
public class Dish {

    private Long id;                // 菜品ID（主键）

    private String name;            // 菜品名称

    private String description;     // 菜品描述

    private BigDecimal price;       // 菜品价格

    private String image;           // 菜品图片地址

    private Integer status;         // 菜品状态（0售罄 1在售）

    private Integer sales;          // 月销量

    private Long categoryId;        // 分类ID

    private Long merchantId;        // 商家ID

    private LocalDateTime createTime;   // 创建时间

    private LocalDateTime updateTime;   // 更新时间
}