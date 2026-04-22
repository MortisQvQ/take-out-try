package com.example.takeouttry.service;

import com.example.takeouttry.DTO.AddCartDTO;
import com.example.takeouttry.DTO.CartMerchantVO;
import com.example.takeouttry.DTO.CartSummaryDTO;
import com.example.takeouttry.DTO.UpdateCartDTO;

import java.util.List;

/**
 * 购物车服务接口
 */
public interface CartService {

    /**
     * 加购 / 更新数量
     * @param userId 当前登录用户ID（从 SecurityContext 或 token 获取）
     * @param dto 加购参数
     */
    void addToCart(Long userId, AddCartDTO dto);

    /**
     * 修改购物车项（数量 / 选中状态）
     * @param userId 用户ID（用于权限校验）
     * @param cartId 购物车项ID
     * @param dto 更新参数（quantity 和 selected 可选，部分更新）
     */
    void updateCartItem(Long userId, Long cartId, UpdateCartDTO dto);

    /**
     * 删除单条购物车记录
     * @param userId 用户ID
     * @param cartId 购物车项ID
     */
    void removeFromCart(Long userId, Long cartId);

    /**
     * 清空用户在某商家的全部购物车（或全部，如果 merchantId 为 null）
     * @param userId 用户ID
     * @param merchantId 商家ID（可选）
     */
    void clearCart(Long userId, Long merchantId);

    /**
     * 获取用户购物车列表（按商家分组）
     * @param userId 用户ID
     * @return 按商家分组的购物车数据
     */
    List<CartMerchantVO> listCartGrouped(Long userId);

    /**
     * 获取购物车汇总（选中商品的总数量 + 总金额）
     * @param userId 用户ID
     * @return 汇总信息
     */
    CartSummaryDTO getCartSummary(Long userId);
}