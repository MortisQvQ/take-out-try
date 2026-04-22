package com.example.takeouttry.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单明细（订单项）实体
 */
@Data
public class OrderItem {

    private Long id;

    private Long orderId;           // 所属订单ID

    private Long dishId;            // 菜品ID

    // 快照字段（下单瞬间的信息，防止后续菜品改价/删除影响订单）
    private String dishName;        // 菜品名称快照
    private BigDecimal price;       // 下单时的单价快照

    private Integer quantity;       // 购买数量

    private LocalDateTime createTime;
}