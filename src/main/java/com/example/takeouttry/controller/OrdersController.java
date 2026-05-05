package com.example.takeouttry.controller;

import com.example.takeouttry.DTO.*;
import com.example.takeouttry.entity.Orders;
import com.example.takeouttry.security.JwtUser;
import com.example.takeouttry.service.AUserService;
import com.example.takeouttry.service.OrdersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orders")
public class OrdersController {

    private final OrdersService ordersService;

    @Autowired
    private AUserService aUserService;

    // 构造器注入
    public OrdersController(OrdersService ordersService) {
        this.ordersService = ordersService;
    }

    /**
     * 创建订单（从当前用户选中的购物车商品生成）
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody Map<String, Object> param) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof JwtUser)) {
            throw new IllegalArgumentException("未登录或认证信息无效");
        }
        Long userId = ((JwtUser) principal).getId();

        Long merchantId;
        if (!param.containsKey("merchantId") || param.get("merchantId") == null) {
            throw new IllegalArgumentException("请选择要结算的商家（merchantId 必填）");
        }
        try {
            merchantId = Long.parseLong(param.get("merchantId").toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("merchantId 格式错误，必须是数字");
        }

        // addressId 真正可选，允许为 null
        Long addressId = null;
        if (param.containsKey("addressId") && param.get("addressId") != null) {
            try {
                addressId = Long.parseLong(param.get("addressId").toString());
            } catch (NumberFormatException ignored) {
                addressId = null;
            }
        }

        // remark 可选
        String remark = null;
        if (param.containsKey("remark") && param.get("remark") != null) {
            remark = param.get("remark").toString().trim();
            if (remark.length() > 255) {
                throw new IllegalArgumentException("备注长度不能超过255个字符");
            }
        }

        Orders created = ordersService.createOrderFromCart(userId, merchantId, addressId, remark);

        Map<String, Object> result = new HashMap<>();
        result.put("orderId", created.getId());
        result.put("orderNo", created.getOrderNo());
        result.put("totalAmount", created.getTotalAmount());
        result.put("status", created.getStatus());

        return ResponseEntity.ok(result);
    }
    /**
     * 用户支付订单
     */
    @PostMapping("/pay/{orderNo}")
    public ResponseEntity<Map<String, Object>> payOrder(@PathVariable String orderNo) {
        Long userId = getLoginUserId();
        ordersService.payOrder(userId, orderNo);

        Map<String, Object> result = new HashMap<>();
        result.put("message", "支付成功");
        result.put("orderNo", orderNo);
        return ResponseEntity.ok(result);
    }

    /**
     * 商家接单
     */
    @PostMapping("/merchant/accept/{orderNo}")
    public ResponseEntity<Map<String, Object>> acceptOrder(@PathVariable String orderNo) {
        Long merchantUserId = getLoginUserId();
        ordersService.acceptOrder(merchantUserId, orderNo);

        Map<String, Object> result = new HashMap<>();
        result.put("message", "接单成功");
        result.put("orderNo", orderNo);
        return ResponseEntity.ok(result);
    }

    /**
     * 商家完成订单
     */
    @PostMapping("/merchant/complete/{orderNo}")
    public ResponseEntity<Map<String, Object>> completeOrder(@PathVariable String orderNo) {
        Long merchantUserId = getLoginUserId();
        ordersService.completeOrder(merchantUserId, orderNo);

        Map<String, Object> result = new HashMap<>();
        result.put("message", "订单已完成");
        result.put("orderNo", orderNo);
        return ResponseEntity.ok(result);
    }

    /**
     * 用户取消订单
     */
    @PostMapping("/cancel/{orderNo}")
    public ResponseEntity<Map<String, Object>> cancelOrder(@PathVariable String orderNo) {
        Long userId = getLoginUserId();
        ordersService.cancelOrder(userId, orderNo);

        Map<String, Object> result = new HashMap<>();
        result.put("message", "订单已取消");
        result.put("orderNo", orderNo);
        return ResponseEntity.ok(result);
    }

    /**
     * 商家端：分页查询订单列表
     */
    @GetMapping("/merchant/list")
    public Result<PageResult<OrderVO>> pageMerchantOrders(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) List<Integer> statusList,
            @AuthenticationPrincipal JwtUser jwtUser) {

        if (jwtUser == null) {
            throw new IllegalArgumentException("未登录或认证信息无效");
        }

        if (pageNum < 1) pageNum = 1;
        if (pageSize < 1 || pageSize > 50) pageSize = 10;

        PageResult<OrderVO> result = ordersService.pageMerchantOrders(
                jwtUser.getId(), pageNum, pageSize, statusList);

        return Result.success(result);
    }

    // ==================== 统计相关接口（已直接整合） ====================

    /**
     * 商家端：订单统计概览
     * GET /orders/merchant/statistics
     */
    @GetMapping("/merchant/statistics")
    public Result<OrderStatisticsVO> getOrderStatistics(
            @AuthenticationPrincipal JwtUser jwtUser) {

        if (jwtUser == null) {
            return Result.error("未登录或认证信息无效", 401);
        }

        try {
            OrderStatisticsVO statistics = ordersService.getOrderStatistics(jwtUser.getId());
            return Result.success(statistics);
        } catch (Exception e) {
            System.err.println("获取订单统计失败: " + e.getMessage());
            return Result.error("获取统计数据失败，请稍后重试", 500);
        }
    }

    /**
     * 商家端：菜品销量排行 Top N
     * GET /orders/merchant/statistics/dish-sales?topN=5
     */
    @GetMapping("/merchant/statistics/dish-sales")
    public Result<List<DishSalesVO>> getDishSalesTop(
            @RequestParam(defaultValue = "5") int topN,
            @AuthenticationPrincipal JwtUser jwtUser) {

        if (jwtUser == null) {
            return Result.error("未登录或认证信息无效", 401);
        }

        // 参数防护
        if (topN < 1) topN = 5;
        if (topN > 50) topN = 50;

        try {
            List<DishSalesVO> dishSales = ordersService.getDishSalesTop(jwtUser.getId(), topN);
            return Result.success(dishSales);
        } catch (Exception e) {
            System.err.println("获取菜品销量排行失败: " + e.getMessage());
            return Result.error("获取菜品销量排行失败，请稍后重试", 500);
        }
    }

    /**
     * 私有辅助方法：获取当前登录用户 ID
     */
    private Long getLoginUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof JwtUser)) {
            throw new IllegalArgumentException("未登录或认证信息无效");
        }
        return ((JwtUser) principal).getId();
    }


    /**
     * 用户端：查询自己的订单列表（支持状态筛选）
     */
    @GetMapping("/user/list")
    public Result<PageResult<OrderVO>> pageUserOrders(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) List<Integer> statusList,
            @AuthenticationPrincipal JwtUser jwtUser) {

        if (jwtUser == null) {
            return Result.error("未登录或认证信息无效", 401);
        }

        if (pageNum < 1) pageNum = 1;
        if (pageSize < 1 || pageSize > 50) pageSize = 10;

        PageResult<OrderVO> result = ordersService.pageUserOrders(
                jwtUser.getId(), pageNum, pageSize, statusList);

        return Result.success(result);
    }

    /**
     * 获取当前登录用户的累计订单数和累计消费金额（用于个人中心）
     */
    @GetMapping("/user/statistics")
    public Result<UserOrderStatisticsVO> getUserOrderStatistics(
            @AuthenticationPrincipal JwtUser user) {

        if (user == null) {
            return Result.error("请先登录", 401);
        }

        try {
            UserOrderStatisticsVO stats = ordersService.getUserOrderStatistics(user.getId());
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("获取个人消费统计失败：" + e.getMessage(), 500);
        }
    }
}