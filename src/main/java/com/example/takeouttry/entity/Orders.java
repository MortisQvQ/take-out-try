package com.example.takeouttry.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单主表实体
 */
@Data
public class Orders {

    private Long id;

    private String orderNo;         // 订单编号（唯一）

    private Long userId;            // 下单用户ID
    private Long merchantId;        // 商家ID
    private Long addressId;         // 收货地址ID（可为空）

    private BigDecimal totalAmount; // 订单总金额

    private Integer status;         // 订单状态：0=待支付 1=已支付 2=已接单 3=已完成 4=已取消

    private LocalDateTime payTime;  // 支付时间

    private String remark;          // 用户备注

    private Integer hasComment;     //是否已评价：0=未评价，1=已评价

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}