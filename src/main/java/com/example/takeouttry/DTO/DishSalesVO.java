package com.example.takeouttry.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 菜品销量统计VO（用于商家端菜品销量排行）
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DishSalesVO {

    /**
     * 菜品ID
     */
    private Long dishId;

    /**
     * 菜品名称（快照）
     */
    private String dishName;

    /**
     * 总销量（份数）
     */
    private Integer totalQuantity;

    /**
     * 总销售额
     */
    private BigDecimal totalSalesAmount;
}
