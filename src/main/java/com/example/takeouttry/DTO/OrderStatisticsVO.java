package com.example.takeouttry.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单统计VO（商家端销量统计）
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderStatisticsVO {

    /**
     * 已完成订单总数
     */
    private Integer totalCompleted;

    /**
     * 已完成订单总金额
     */
    private BigDecimal totalAmount;

    /**
     * 总售出菜品数量
     */
    private Integer totalDishCount;

    /**
     * 待支付订单数
     */
    private Integer pendingPayment;

    /**
     * 待接单订单数
     */
    private Integer pendingAccept;

    /**
     * 已接单（进行中）订单数
     */
    private Integer accepted;

    /**
     * 已取消订单数
     */
    private Integer cancelled;

    /**
     * 最近30天日期（x轴） - 格式 "4/19"
     */
    private List<String> trendDates;

    /**
     * 最近30天每日营业额（y轴）
     */
    private List<BigDecimal> trendAmounts;

    /**
     * 已完成订单数趋势（百分比，正数为上升，负数为下降）
     */
    private Double completedTrend;

    /**
     * 营业额趋势百分比
     */
    private Double amountTrend;

    /**
     * 售出菜品趋势百分比
     */
    private Double dishCountTrend;
}
