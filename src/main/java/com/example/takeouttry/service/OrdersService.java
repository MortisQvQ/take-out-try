package com.example.takeouttry.service;

import com.example.takeouttry.DTO.*;
import com.example.takeouttry.entity.Orders;

import java.util.List;

public interface OrdersService {

    /**
     * 从购物车创建订单（指定单一商家）
     *
     * @param userId     用户ID
     * @param merchantId 要结算的商家ID（必填）
     * @param addressId  收货地址ID（可选）
     * @param remark     备注（可选）
     * @return 创建后的订单
     */
    Orders createOrderFromCart(Long userId, Long merchantId, Long addressId, String remark);

    /**
     * 支付订单
     * @param userId 操作用户ID
     * @param orderNo 雪花算法生成的订单编号
     */
    void payOrder(Long userId, String orderNo);

    /**
     * 商家接单
     * @param merchantUserId 商家关联的用户ID
     * @param orderNo 雪花算法生成的订单编号
     */
    void acceptOrder(Long merchantUserId, String orderNo);

    /**
     * 完成订单
     * @param merchantUserId 商家关联的用户ID
     * @param orderNo 雪花算法生成的订单编号
     */
    void completeOrder(Long merchantUserId, String orderNo);

    /**
     * 用户取消订单
     * @param userId 当前登录用户ID
     * @param orderNo 订单编号
     */
    void cancelOrder(Long userId, String orderNo);


    /*/**
     * 商家端：根据状态列表查询订单（含菜品明细）
     * @param userId     当前登录的用户ID
     * @param statusList 想要查询的状态集合（例如：[1]代表待接单，[2]代表进行中）
     * @return 包含菜品明细的订单列表
     */
    /*List<OrderVO> listMerchantOrders(Long userId, List<Integer> statusList);
     */

    /**
     * 商家端：分页查询订单列表（含菜品明细）
     * @param pageNum    当前页码
     * @param pageSize   每页条数
     * @param statusList 状态筛选集合
     * @return 封装了 OrderVO 列表及分页元数据的 PageResult
     */
    PageResult<OrderVO> pageMerchantOrders(Long merchantUserId, int pageNum, int pageSize, List<Integer> statusList);

    // ==================== 统计相关 ====================

    /**
     * 商家端：获取订单统计概览
     * 包含各状态订单数量、已完成订单总金额、总售出菜品数
     *
     * @param merchantUserId 商家关联的用户ID
     * @return 订单统计VO
     */
    OrderStatisticsVO getOrderStatistics(Long merchantUserId);

    /**
     * 商家端：获取菜品销量排行 Top N
     * 只统计已完成订单中的菜品销量
     *
     * @param merchantUserId 商家关联的用户ID
     * @param topN           取前几名（默认5）
     * @return 菜品销量排行列表
     */
    List<DishSalesVO> getDishSalesTop(Long merchantUserId, int topN);

    PageResult<OrderVO> pageUserOrders(Long userId, int pageNum, int pageSize, List<Integer> statusList);

    /**
     * 获取指定用户的累计订单数和累计消费总额
     * @param userId 用户ID
     * @return 统计数据
     */
    UserOrderStatisticsVO getUserOrderStatistics(Long userId);


}
