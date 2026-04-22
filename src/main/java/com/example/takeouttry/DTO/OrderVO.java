package com.example.takeouttry.DTO;

import com.example.takeouttry.entity.OrderItem;
import com.example.takeouttry.entity.Orders;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)   // 继承时推荐加上这行，避免 lombok 警告
public class OrderVO extends Orders {

    /**
     * 订单关联的菜品明细列表
     */
    private List<OrderItem> orderItems;

    // ==================== 商家端友好字段（新增） ====================

    /**
     * 订单状态描述（前端直接显示用）
     * 示例：待接单、已接单、已完成、已取消
     */
    private String statusDesc;

    /**
     * 菜品总数量（方便商家快速查看）
     */
    private Integer totalQuantity;

    /**
     * 是否允许接单（根据状态动态判断）
     */
    private Boolean canAccept;

    /**
     * 是否允许完成订单
     */
    private Boolean canComplete;

    private String merchantName;

}