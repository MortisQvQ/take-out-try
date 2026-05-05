package com.example.takeouttry.mapper;

import com.example.takeouttry.DTO.DailySalesDTO;
import com.example.takeouttry.DTO.UserOrderStatisticsVO;
import com.example.takeouttry.entity.Orders;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author 33126
 * @description 针对表【orders(订单表)】的数据库操作Mapper
 * @createDate 2026-03-19 21:15:50
 * @Entity .Orders
 */
@Mapper
public interface OrdersMapper {

    /**
     * 新增订单（返回自增主键）
     */
    int insert(Orders orders);

    /**
     * 根据订单ID查询（单条）
     */
    Orders selectById(@Param("id") Long id);

    /**
     * 根据订单编号查询（唯一索引）
     */
    Orders selectByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 根据用户ID查询某个用户的最新一条订单（示例，可根据需要加 limit 或条件）
     */
    Orders selectByUserId(@Param("userId") Long userId);

    /**
     * 根据主键更新
     */
    int updateById(Orders orders);

    /**
     * 根据主键删除（物理删除）
     */
    int deleteById(@Param("id") Long id);

    // 可选扩展方法（后续按需添加）
    // List<Orders> selectByUserIdAndStatus(@Param("userId") Long userId, @Param("status") Integer status);

    //List<Orders> selectByMerchantAndStatus(@Param("merchantId") Long merchantId, @Param("statusList") List<Integer> statusList);

    /**
     * 分页查询指定商家的订单主表
     * @param merchantId 商家ID
     * @param statusList 状态列表（1,2,3...）
     * @param offset     偏移量 (pageNum-1)*pageSize
     * @param pageSize   每页条数
     */
    List<Orders> listMerchantOrdersWithPage(@Param("merchantId") Long merchantId,
                                            @Param("statusList") List<Integer> statusList,
                                            @Param("offset") int offset,
                                            @Param("pageSize") int pageSize);

    /**
     * 查询符合条件的订单总数
     */
    long countMerchantOrders(@Param("merchantId") Long merchantId,
                             @Param("statusList") List<Integer> statusList);

    // ==================== 统计相关 ====================

    /**
     * 按状态统计指定商家的订单数量
     * @param merchantId 商家ID
     * @param status     订单状态
     * @return 该状态下的订单数量
     */
    int countByMerchantAndStatus(@Param("merchantId") Long merchantId,
                                 @Param("status") int status);

    /**
     * 统计指定商家已完成订单的总金额
     * @param merchantId 商家ID
     * @return 已完成订单总金额
     */
    BigDecimal sumCompletedAmount(@Param("merchantId") Long merchantId);

    /**
     * 统计指定商家已完成订单的总售出菜品数量
     * @param merchantId 商家ID
     * @return 总售出菜品份数
     */
    Integer sumCompletedDishCount(@Param("merchantId") Long merchantId);

    // 用户端：分页查询自己的订单
    List<Orders> listUserOrdersWithPage(@Param("userId") Long userId,
                                        @Param("statusList") List<Integer> statusList,
                                        @Param("offset") int offset,
                                        @Param("pageSize") int pageSize);

    // 用户端：统计订单总数
    long countUserOrders(@Param("userId") Long userId,
                         @Param("statusList") List<Integer> statusList);

    /**
     * 查询商家最近30天每日已完成订单的营业额（用于趋势图）
     * @param merchantId 商家ID
     * @return 每天的日期和金额
     */
    List<DailySalesDTO> getDailySalesLast30Days(@Param("merchantId") Long merchantId);

    // 根据时间区间统计已完成订单数
    Integer countCompletedInRange(@Param("merchantId") Long merchantId,
                                  @Param("begin") LocalDateTime begin,
                                  @Param("end") LocalDateTime end);

    // 根据时间区间统计营业额
    BigDecimal sumAmountInRange(@Param("merchantId") Long merchantId,
                                @Param("begin") LocalDateTime begin,
                                @Param("end") LocalDateTime end);

    // 根据时间区间统计菜品售出数
    Integer sumDishCountInRange(@Param("merchantId") Long merchantId,
                                @Param("begin") LocalDateTime begin,
                                @Param("end") LocalDateTime end);


    /**
     * 更新订单的评价状态
     * @param orderId 订单ID
     * @param hasComment 评价状态：0=未评价，1=已评价
     * @return 影响行数
     */
    int updateHasCommentStatus(@Param("id") Long orderId, @Param("hasComment") Integer hasComment);

    /**
     * 聚合查询用户的订单数及累计消费额
     */
    UserOrderStatisticsVO selectStatisticsByUserId(@Param("userId") Long userId);


}
