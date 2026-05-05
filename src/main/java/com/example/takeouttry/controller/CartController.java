package com.example.takeouttry.controller;

import com.example.takeouttry.DTO.*;
import com.example.takeouttry.security.JwtUser;
import com.example.takeouttry.service.CartService;
import com.example.takeouttry.DTO.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 购物车控制器
 */
@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /**
     * 加购 / 累加数量
     * - 如果已存在同商家同菜品，则数量累加
     * - 默认选中
     */
    @PostMapping("/add")
    public Result<Void> addToCart(
            @AuthenticationPrincipal JwtUser user,
            @RequestBody @Valid AddCartDTO dto) {

       if (user == null) {
            return Result.error("请先登录", 401);
        }

        try {
            cartService.addToCart(user.getId(), dto);
            return Result.success(null, "添加购物车成功");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage(), 400);
        } catch (Exception e) {
            return Result.error("添加失败：" + e.getMessage(), 500);
        }
    }

    /**
     * 修改购物车项（数量 / 选中状态）
     * - 支持部分更新（只传 quantity 或只传 selected 都行）
     */
    @PutMapping("/{cartId}")
    public Result<Void> updateCartItem(
            @AuthenticationPrincipal JwtUser user,
            @PathVariable Long cartId,
            @RequestBody UpdateCartDTO dto) {

        if (user == null) {
            return Result.error("请先登录", 401);
        }

        try {
            cartService.updateCartItem(user.getId(), cartId, dto);
            return Result.success(null, "更新成功");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage(), 400);
        } catch (Exception e) {
            return Result.error("更新失败：" + e.getMessage(), 500);
        }
    }

    /**
     * 删除单条购物车记录
     */
    @DeleteMapping("/{cartId}")
    public Result<Void> removeFromCart(
            @AuthenticationPrincipal JwtUser user,
            @PathVariable Long cartId) {

        if (user == null) {
            return Result.error("请先登录", 401);
        }

        try {
            cartService.removeFromCart(user.getId(), cartId);
            return Result.success(null, "删除成功");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage(), 400);
        } catch (Exception e) {
            return Result.error("删除失败：" + e.getMessage(), 500);
        }
    }

    /**
     * 清空购物车
     * - merchantId 不传或 <=0：清空当前用户全部
     * - merchantId >0：只清空该商家的
     */
    @DeleteMapping("/clear")
    public Result<Void> clearCart(
            @AuthenticationPrincipal JwtUser user,
            @RequestParam(required = false) Long merchantId) {

        if (user == null) {
            return Result.error("请先登录", 401);
        }

        try {
            cartService.clearCart(user.getId(), merchantId);
            return Result.success(null, "清空成功");
        } catch (Exception e) {
            return Result.error("清空失败：" + e.getMessage(), 500);
        }
    }

    /**
     * 获取购物车列表（按商家分组）
     * - 返回 List<CartMerchantVO>，每个商家一个分组
     * - 包含商家信息 + 商品列表 + 选中商品小计
     */
    @GetMapping("/list")
    public Result<List<CartMerchantVO>> listCart(
            @AuthenticationPrincipal JwtUser user) {

        if (user == null) {
            return Result.error("请先登录", 401);
        }

        try {
            List<CartMerchantVO> list = cartService.listCartGrouped(user.getId());
            return Result.success(list);
        } catch (Exception e) {
            return Result.error("查询购物车失败：" + e.getMessage(), 500);
        }
    }

    /**
     * 获取购物车汇总（只算选中的商品）
     * - totalQuantity：选中商品总件数
     * - totalAmount：选中商品总金额
     */
    @GetMapping("/summary")
    public Result<CartSummaryDTO> getCartSummary(
            @AuthenticationPrincipal JwtUser user) {

        if (user == null) {
            return Result.error("请先登录", 401);
        }

        try {
            CartSummaryDTO summary = cartService.getCartSummary(user.getId());
            return Result.success(summary);
        } catch (Exception e) {
            return Result.error("获取汇总失败：" + e.getMessage(), 500);
        }
    }

    /**
     * 可选扩展：设置某商家所有商品的选中状态（全选/全不选）
     *   cartMapper.updateSelectedByUserAndMerchant(userId, merchantId, selected);
     */
    @PutMapping("/select")
    public Result<Void> selectAllByMerchant(
            @AuthenticationPrincipal JwtUser user,
            @RequestParam Long merchantId,
            @RequestParam Integer selected) {

        if (user == null) {
            return Result.error("请先登录", 401);
        }

        if (merchantId == null || merchantId <= 0) {
            return Result.error("商家ID不能为空", 400);
        }

        if (selected != 0 && selected != 1) {
            return Result.error("选中状态只能为0或1", 400);
        }

        return Result.success(null, "功能待实现，可在 CartServiceImpl 中添加 updateSelectedByUserAndMerchant 调用");
    }

    /**
     * 获取购物车所有商品的总数量（包含未选中的商品）
     */
    @GetMapping("/total-count")
    public Result<Integer> getCartTotalCount(@AuthenticationPrincipal JwtUser user) {
        if (user == null) {
            return Result.error("请先登录", 401);
        }
        try {
            Integer totalCount = cartService.getCartTotalCount(user.getId());
            return Result.success(totalCount);
        } catch (Exception e) {
            return Result.error("获取购物车商品总数失败：" + e.getMessage(), 500);
        }
    }
}