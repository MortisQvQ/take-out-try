package com.example.takeouttry.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 购物车实体
 */
@Data
public class Cart {

    private Long id;

    private Long userId;
    private Long merchantId;
    private Long dishId;

    private Integer quantity;
    private Integer selected;      // 1=选中 0=未选中

    private BigDecimal unitPrice;  // 加入时的单价快照

    // 快照字段（推荐保留，减少联表）
    private String dishName;
    private String dishImage;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}