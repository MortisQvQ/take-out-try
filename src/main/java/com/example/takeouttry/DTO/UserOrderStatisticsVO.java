package com.example.takeouttry.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * 用户订单统计数据对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserOrderStatisticsVO {
    /**
     * 累计订单总数（只统计已支付/已接单/已完成的有效订单，排除待支付和已取消）
     */
    private Integer totalOrderCount;

    /**
     * 累计消费总金额（只统计有效订单金额）
     */
    private BigDecimal totalExpenditure;
}