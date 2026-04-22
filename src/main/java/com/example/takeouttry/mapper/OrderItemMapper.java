package com.example.takeouttry.mapper;

import com.example.takeouttry.DTO.DishSalesVO;
import com.example.takeouttry.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @description 订单明细表 order_item 的Mapper
 */
@Mapper
public interface OrderItemMapper {

    /**
     * 新增一条订单明细
     */
    int insert(OrderItem orderItem);

    /**
     * 批量新增订单明细（推荐在下单时使用）
     */
    int insertBatch(@Param("list") List<OrderItem> list);

    /**
     * 根据主键查询单条明细
     */
    OrderItem selectById(@Param("id") Long id);

    /**
     * 根据订单ID查询该订单的所有明细项（最常用）
     */
    List<OrderItem> selectByOrderId(@Param("orderId") Long orderId);

    /**
     * 根据主键更新（通常用于修改数量等）
     */
    int updateById(OrderItem orderItem);

    /**
     * 根据主键删除单条明细
     */
    int deleteById(@Param("id") Long id);

    /**
     * 根据订单ID删除所有明细（订单取消/删除时使用）
     */
    int deleteByOrderId(@Param("orderId") Long orderId);

    // 可选扩展
    // int updateQuantityById(@Param("id") Long id, @Param("quantity") Integer quantity);

    // ==================== 统计相关 ====================

    /**
     * 查询指定商家已完成订单的菜品销量排行（Top N）
     * 关联 orders 表筛选 status=3（已完成）的订单，按菜品分组统计
     *
     * @param merchantId 商家ID
     * @param topN       取前几名
     * @return 菜品销量排行列表
     */
    List<DishSalesVO> selectDishSalesTop(@Param("merchantId") Long merchantId,
                                         @Param("topN") int topN);


}