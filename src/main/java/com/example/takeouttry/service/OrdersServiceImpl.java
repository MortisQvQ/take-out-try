package com.example.takeouttry.service;

import com.example.takeouttry.DTO.*;
import com.example.takeouttry.common.SnowflakeIdWorker;
import com.example.takeouttry.entity.*;
import com.example.takeouttry.mapper.CartMapper;
import com.example.takeouttry.mapper.MerchantMapper;
import com.example.takeouttry.mapper.OrderItemMapper;
import com.example.takeouttry.mapper.OrdersMapper;
import com.example.takeouttry.security.JwtUser;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
//import java.util.Collections;
import java.util.Collections;
import java.util.List;

@Service
public class OrdersServiceImpl implements OrdersService {

    private final OrdersMapper ordersMapper;
    private final OrderItemMapper orderItemMapper;
    private final CartMapper cartMapper;
    private final MerchantMapper merchantMapper; // 新增注入
    private final SnowflakeIdWorker idWorker;

    public OrdersServiceImpl(
            OrdersMapper ordersMapper,
            OrderItemMapper orderItemMapper,
            CartMapper cartMapper,
            MerchantMapper merchantMapper) { // 构造器注入
        this.ordersMapper = ordersMapper;
        this.orderItemMapper = orderItemMapper;
        this.cartMapper = cartMapper;
        this.merchantMapper = merchantMapper;
        // 单机环境，workerId 和 datacenterId 写死无分布式冲突
        this.idWorker = new SnowflakeIdWorker(1, 1);
    }

    /**
     * 【原功能保留】创建订单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Orders createOrderFromCart(Long userId, Long merchantId, Long addressId, String remark) {
        // 参数校验：merchantId 必填
        if (merchantId == null) {
            throw new IllegalArgumentException("请选择要结算的商家");
        }

        // 备注长度校验
        if (remark != null && remark.length() > 255) {
            throw new IllegalArgumentException("备注长度不能超过255个字符");
        }

        // 1. 查询指定商家的选中购物车项
        List<Cart> selectedCarts = cartMapper.selectByUserIdAndMerchantAndSelected(userId, merchantId, 1);
        if (selectedCarts == null || selectedCarts.isEmpty()) {
            throw new IllegalArgumentException("该商家购物车中没有选中的商品，无法创建订单");
        }

        // 2. 计算订单总金额（菜品合计）
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (Cart cart : selectedCarts) {
            BigDecimal itemTotal = cart.getUnitPrice().multiply(BigDecimal.valueOf(cart.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);
        }

        if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("订单金额必须大于0");
        }

        // 3. 生成订单号（纯雪花算法）
        String orderNo = String.valueOf(idWorker.nextId());

        // 4. 插入订单主表
        Orders order = new Orders();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setMerchantId(merchantId);
        order.setAddressId(addressId);
        order.setTotalAmount(totalAmount);
        order.setStatus(OrderStatus.PENDING_PAYMENT.getCode());  // 待支付 (0)
        order.setRemark(remark);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());

        ordersMapper.insert(order);

        // 5. 批量插入订单明细
        List<OrderItem> items = new ArrayList<>();
        for (Cart cart : selectedCarts) {
            OrderItem item = new OrderItem();
            item.setOrderId(order.getId());
            item.setDishId(cart.getDishId());
            item.setDishName(cart.getDishName());
            item.setPrice(cart.getUnitPrice());
            item.setQuantity(cart.getQuantity());
            item.setCreateTime(LocalDateTime.now());
            items.add(item);
        }
        orderItemMapper.insertBatch(items);

        // 6. 只删除该商家的选中购物车记录
        cartMapper.deleteByUserIdAndMerchantAndSelected(userId, merchantId, 1);

        return order;
    }

    /**
     * 【新加功能】用户支付
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payOrder(Long userId, String orderNo) {
        Orders order = ordersMapper.selectByOrderNo(orderNo);

        if (order == null) throw new IllegalArgumentException("订单不存在");
        if (!userId.equals(order.getUserId())) throw new IllegalArgumentException("无权操作此订单");

        if (order.getStatus() != OrderStatus.PENDING_PAYMENT.getCode()) {
            throw new IllegalStateException("订单状态异常，无法支付");
        }

        order.setStatus(OrderStatus.PAID.getCode()); // 已支付(1)
        order.setPayTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());

        ordersMapper.updateById(order);
    }

    /**
     * 【新加功能】商家接单
     * 校验逻辑：通过订单里的 merchantId 换算成老板的 userId 进行比对
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void acceptOrder(Long merchantUserId, String orderNo) {
        Orders order = ordersMapper.selectByOrderNo(orderNo);
        if (order == null) throw new IllegalArgumentException("订单不存在");

        // 身份换算：根据订单的店铺ID查出老板的用户ID
        Merchant merchant = merchantMapper.selectById(order.getMerchantId());
        if (merchant == null) throw new IllegalArgumentException("关联商家记录不存在");

        // 权限校验：比对当前登录 ID 是否等于老板 ID
        if (!merchantUserId.equals(merchant.getUserId())) {
            throw new IllegalArgumentException("无权操作此订单：您不是该店铺的负责人");
        }

        if (order.getStatus() != OrderStatus.PAID.getCode()) {
            throw new IllegalStateException("订单尚未支付，无法接单");
        }

        order.setStatus(OrderStatus.ACCEPTED.getCode()); // 已接单(2)
        order.setUpdateTime(LocalDateTime.now());
        ordersMapper.updateById(order);
    }

    /**
     * 【新加功能】订单完成
     * 校验逻辑：同接单逻辑，确保只有该店老板能点完成
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeOrder(Long merchantUserId, String orderNo) {
        Orders order = ordersMapper.selectByOrderNo(orderNo);
        if (order == null) throw new IllegalArgumentException("订单不存在");

        // 身份换算
        Merchant merchant = merchantMapper.selectById(order.getMerchantId());
        if (merchant == null) throw new IllegalArgumentException("关联商家记录不存在");

        // 权限校验
        if (!merchantUserId.equals(merchant.getUserId())) {
            throw new IllegalArgumentException("无权操作此订单：您不是该店铺的负责人");
        }

        if (order.getStatus() != OrderStatus.ACCEPTED.getCode()) {
            throw new IllegalStateException("商家尚未接单，无法完成");
        }

        order.setStatus(OrderStatus.COMPLETED.getCode()); // 已完成(3)
        order.setUpdateTime(LocalDateTime.now());
        ordersMapper.updateById(order);
    }

    /**
     * 取消订单
     * @param userId 当前登录用户ID
     * @param orderNo 订单编号
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long userId, String orderNo) {
        // 1. 查询订单
        Orders order = ordersMapper.selectByOrderNo(orderNo);
        if (order == null) throw new IllegalArgumentException("订单不存在");

        // 2. 权限校验：只能取消自己的订单
        if (!userId.equals(order.getUserId())) {
            throw new IllegalArgumentException("无权操作此订单");
        }

        // 3. 状态校验：只有状态为 0(待支付) 或 1(已支付) 时可以取消
        // 如果状态已经是 2(已接单) 或 3(已完成)，则不能取消
        if (order.getStatus() >= OrderStatus.ACCEPTED.getCode()) {
            throw new IllegalStateException("商家已接单或订单已完成，无法取消。请联系商家协商。");
        }

        // 如果已经是取消状态，直接返回即可（幂等处理）
        if (order.getStatus() == 4) return;

        // 4. 更新状态为已取消 (4)
        order.setStatus(4);
        order.setUpdateTime(LocalDateTime.now());
        ordersMapper.updateById(order);

        // 💡 进阶思考：如果是已支付(1)状态下取消，这里还需要触发【原路退款】逻辑
        // if (order.getStatus() == OrderStatus.PAID.getCode()) {
        //     refundService.refund(order);
        // }
    }

    @Override
    public PageResult<OrderVO> pageMerchantOrders(Long merchantUserId, int pageNum, int pageSize, List<Integer> statusList) {

        // 1. 权限转换：userId → merchantId
        Merchant merchant = merchantMapper.selectByUserId(merchantUserId);
        if (merchant == null) {
            return PageResult.<OrderVO>builder()
                    .list(Collections.emptyList())
                    .total(0L)
                    .pages(0)
                    .pageNum(pageNum)
                    .pageSize(pageSize)
                    .build();
        }

        // 2. 分页参数处理
        int offset = (pageNum - 1) * pageSize;

        // 3. 查询总条数
        long total = ordersMapper.countMerchantOrders(merchant.getId(), statusList);

        // 4. 计算总页数
        int pages = total == 0 ? 0 : (int) Math.ceil((double) total / pageSize);

        // 5. 查询当前页订单主表
        List<Orders> ordersList = ordersMapper.listMerchantOrdersWithPage(
                merchant.getId(), statusList, offset, pageSize);

        // 6. 聚合 OrderVO + 订单明细（当前阶段保留循环，清晰易维护）
        List<OrderVO> voList = new ArrayList<>(ordersList.size());
        for (Orders order : ordersList) {
            OrderVO vo = new OrderVO();
            BeanUtils.copyProperties(order, vo);

            List<OrderItem> items = orderItemMapper.selectByOrderId(order.getId());
            vo.setOrderItems(items != null ? items : Collections.emptyList());

            // ==================== 商家端字段转换 ====================

            // 设置状态描述（前端显示更友好）
            vo.setStatusDesc(getStatusDesc(order.getStatus()));

            // 计算菜品总数量
            int totalQuantity = items != null
                    ? items.stream().mapToInt(OrderItem::getQuantity).sum()
                    : 0;
            vo.setTotalQuantity(totalQuantity);

            // 设置操作权限（根据状态动态判断）
            vo.setCanAccept(order.getStatus() == 1);   // 已支付状态才能接单
            vo.setCanComplete(order.getStatus() == 2); // 已接单状态才能完成

            // 隐藏对商家不必要的敏感字段
            vo.setUserId(null);

            voList.add(vo);
        }

        // 7. 返回结果
        return PageResult.<OrderVO>builder()
                .list(voList)
                .total(total)
                .pages(pages)
                .pageNum(pageNum)
                .pageSize(pageSize)
                .build();


    }
    /**
     * 根据订单状态码返回中文描述
     */
    private String getStatusDesc(Integer status) {
        if (status == null) {
            return "未知状态";
        }
        return switch (status) {
            case 0 -> "待支付";
            case 1 -> "待接单";
            case 2 -> "已接单";
            case 3 -> "已完成";
            case 4 -> "已取消";
            default -> "未知状态";
        };
    }

    // ==================== 统计相关 ====================

    /**
     * 商家端：获取订单统计概览
     */
    @Override
    public OrderStatisticsVO getOrderStatistics(Long merchantUserId) {
        Merchant merchant = merchantMapper.selectByUserId(merchantUserId);
        if (merchant == null) {
            return OrderStatisticsVO.builder()
                    .totalCompleted(0)
                    .totalAmount(BigDecimal.ZERO)
                    .totalDishCount(0)
                    .pendingPayment(0)
                    .pendingAccept(0)
                    .accepted(0)
                    .cancelled(0)
                    .trendDates(Collections.emptyList())
                    .trendAmounts(Collections.emptyList())
                    .build();
        }

        Long merchantId = merchant.getId();

        // 各状态订单数量
        int unpaid          = ordersMapper.countByMerchantAndStatus(merchantId, 0); // 待支付
        int pendingAccept   = ordersMapper.countByMerchantAndStatus(merchantId, 1); // 待接单
        int accepted        = ordersMapper.countByMerchantAndStatus(merchantId, 2);
        int totalCompleted  = ordersMapper.countByMerchantAndStatus(merchantId, 3);
        int cancelled       = ordersMapper.countByMerchantAndStatus(merchantId, 4);

        BigDecimal totalAmount = ordersMapper.sumCompletedAmount(merchantId);
        int totalDishCount = ordersMapper.sumCompletedDishCount(merchantId);

        // ==================== 最近30天营业额趋势 ====================
        List<DailySalesDTO> dailyList = ordersMapper.getDailySalesLast30Days(merchantId);

        List<String> trendDates = new ArrayList<>();
        List<BigDecimal> trendAmounts = new ArrayList<>();

        for (DailySalesDTO ds : dailyList) {
            trendDates.add(ds.getSaleDate());
            trendAmounts.add(ds.getAmount() != null ? ds.getAmount() : BigDecimal.ZERO);
        }

        // 1. 准备时间区间
        LocalDateTime now = LocalDateTime.now();
        // 本月1号 00:00:00
        LocalDateTime thisMonthStart = now.with(TemporalAdjusters.firstDayOfMonth()).with(LocalTime.MIN);
        // 上月1号 00:00:00
        LocalDateTime lastMonthStart = thisMonthStart.minusMonths(1);
        // 上月今天同一时刻
        LocalDateTime lastMonthSameTime = now.minusMonths(1);

        // 2. 获取本月至今的实时数据（用于计算百分比，保证对比公平）
        BigDecimal thisMonthAmountRealtime = ordersMapper.sumAmountInRange(merchantId, thisMonthStart, now);
        int thisMonthOrdersRealtime = ordersMapper.countCompletedInRange(merchantId, thisMonthStart, now);
        int thisMonthDishesRealtime = ordersMapper.sumDishCountInRange(merchantId, thisMonthStart, now);

        // 3. 获取上月同期数据
        BigDecimal lastMonthAmount = ordersMapper.sumAmountInRange(merchantId, lastMonthStart, lastMonthSameTime);
        int lastMonthOrders = ordersMapper.countCompletedInRange(merchantId, lastMonthStart, lastMonthSameTime);
        int lastMonthDishes = ordersMapper.sumDishCountInRange(merchantId, lastMonthStart, lastMonthSameTime);

        // 4. 计算趋势并填入 VO
        Double amountTrend = calculateTrend(thisMonthAmountRealtime, lastMonthAmount);
        Double completedTrend = calculateTrend(BigDecimal.valueOf(thisMonthOrdersRealtime), BigDecimal.valueOf(lastMonthOrders));
        Double dishCountTrend = calculateTrend(BigDecimal.valueOf(thisMonthDishesRealtime), BigDecimal.valueOf(lastMonthDishes));

        // 返回完整统计 + 趋势数据
        return OrderStatisticsVO.builder()
                .totalCompleted(totalCompleted)
                .totalAmount(totalAmount != null ? totalAmount : BigDecimal.ZERO)
                .totalDishCount(totalDishCount)
                .pendingPayment(unpaid)
                .pendingAccept(pendingAccept)
                .accepted(accepted)
                .cancelled(cancelled)
                .trendDates(trendDates)
                .trendAmounts(trendAmounts)

                .amountTrend(amountTrend)
                .completedTrend(completedTrend)
                .dishCountTrend(dishCountTrend)
                .build();
    }

    /**
     * 趋势计算私有方法（保持严谨）
     */
    private Double calculateTrend(BigDecimal current, BigDecimal previous) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return (current != null && current.compareTo(BigDecimal.ZERO) > 0) ? 100.0 : 0.0;
        }
        if (current == null) current = BigDecimal.ZERO;
        return current.subtract(previous)
                .divide(previous, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal(100))
                .doubleValue();
    }

    /**
     * 商家端：获取菜品销量排行 Top N
     */
    @Override
    public List<DishSalesVO> getDishSalesTop(Long merchantUserId, int topN) {
        Merchant merchant = merchantMapper.selectByUserId(merchantUserId);
        if (merchant == null) {
            return Collections.emptyList();
        }

        // 再次防御性限制（双保险）
        if (topN < 1) topN = 5;
        if (topN > 50) topN = 50;

        return orderItemMapper.selectDishSalesTop(merchant.getId(), topN);
    }


    @Override
    public PageResult<OrderVO> pageUserOrders(Long userId, int pageNum, int pageSize, List<Integer> statusList) {
        if (userId == null) {
            return PageResult.<OrderVO>builder()
                    .list(new ArrayList<>())
                    .total(0L)
                    .pages(0)
                    .pageNum(pageNum)
                    .pageSize(pageSize)
                    .build();
        }

        int offset = (pageNum - 1) * pageSize;

        long total = ordersMapper.countUserOrders(userId, statusList);
        int pages = total == 0 ? 0 : (int) Math.ceil((double) total / pageSize);

        List<Orders> ordersList = ordersMapper.listUserOrdersWithPage(userId, statusList, offset, pageSize);

        List<OrderVO> voList = new ArrayList<>();
        for (Orders order : ordersList) {
            OrderVO vo = new OrderVO();
            BeanUtils.copyProperties(order, vo);

            // 查询订单明细
            List<OrderItem> items = orderItemMapper.selectByOrderId(order.getId());
            vo.setOrderItems(items != null ? items : new ArrayList<>());

            // 计算总数量
            int totalQty = items != null ? items.stream().mapToInt(OrderItem::getQuantity).sum() : 0;
            vo.setTotalQuantity(totalQty);

            // 设置状态描述
            vo.setStatusDesc(getStatusDesc(order.getStatus()));

            // ==================== 新增：查询商家名称 ====================
            if (order.getMerchantId() != null) {
                Merchant merchant = merchantMapper.selectById(order.getMerchantId());
                vo.setMerchantName(merchant != null ? merchant.getName() : "未知商家");
            } else {
                vo.setMerchantName("未知商家");
            }

            voList.add(vo);
        }

        PageResult<OrderVO> result = new PageResult<>();
        result.setList(voList);
        result.setTotal(total);
        result.setPages(pages);
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);

        return result;
    }


    /**
     * 【新加功能】获取普通用户的累计订单数和累计消费金额
     */
    @Override
    public UserOrderStatisticsVO getUserOrderStatistics(Long userId) {
        if (userId == null) {
            return new UserOrderStatisticsVO(0, BigDecimal.ZERO);
        }

        // 1. 调用 Mapper 统计当前用户的有效订单（状态为 1, 2, 3）
        UserOrderStatisticsVO statistics = ordersMapper.selectStatisticsByUserId(userId);

        // 2. 防御性空值处理
        if (statistics == null) {
            return new UserOrderStatisticsVO(0, BigDecimal.ZERO);
        }
        if (statistics.getTotalOrderCount() == null) {
            statistics.setTotalOrderCount(0);
        }
        if (statistics.getTotalExpenditure() == null) {
            statistics.setTotalExpenditure(BigDecimal.ZERO);
        }

        return statistics;
    }


    /*
     * 商家端订单列表分页聚合查询（瀑布流核心接口 - 分页版）
     * 遵循 Dish 分页思路：计算偏移量 + 总数统计 + 数据聚合
     */
    /*@Override
    public PageResult<OrderVO> pageMerchantOrders(int pageNum, int pageSize, List<Integer> statusList) {
        // 1. 获取当前登录老板的用户 ID
        Long userId = getLoginUserId();

        // 2. 权限转换：根据用户 ID 获取其关联的店铺信息
        Merchant merchant = merchantMapper.selectByUserId(userId);
        if (merchant == null) {
            // 如果没开店，返回一个空的 PageResult 对象，各项分页指标设为当前请求值
            PageResult<OrderVO> emptyResult = new PageResult<>();
            emptyResult.setList(new ArrayList<>());
            emptyResult.setTotal(0L);
            emptyResult.setPages(0);
            emptyResult.setPageNum(pageNum);
            emptyResult.setPageSize(pageSize);
            return emptyResult;
        }

        // 3. 计算分页偏移量 (你的核心分页公式：从第几条开始查)
        int offset = (pageNum - 1) * pageSize;

        // 4. 统计总条数 (用于前端瀑布流判断是否加载完毕)
        long total = ordersMapper.countMerchantOrders(merchant.getId(), statusList);

        // 5. 计算总页数 (使用 Math.ceil 向上取整)
        int pages = (int) Math.ceil((double) total / pageSize);

        // 6. 分页抓取主表 (使用带 LIMIT 的新 Mapper 方法)
        List<Orders> ordersList = ordersMapper.listMerchantOrdersWithPage(
                merchant.getId(), statusList, offset, pageSize
        );

        // 7. 数据聚合：循环将 Orders 转换为 OrderVO 并塞入菜品快照
        List<OrderVO> voList = new ArrayList<>();
        if (ordersList != null && !ordersList.isEmpty()) {
            for (Orders order : ordersList) {
                OrderVO vo = new OrderVO();
                // 复制基础属性：订单号、金额、备注、时间等
                BeanUtils.copyProperties(order, vo);

                //  关键操作：查出这笔单子下单瞬间的菜品快照（如：红烧肉x1）
                List<OrderItem> items = orderItemMapper.selectByOrderId(order.getId());
                vo.setOrderItems(items != null ? items : new ArrayList<>());

                voList.add(vo);
            }
        }

        // 8. 组装并返回你定义的 PageResult 结构
        PageResult<OrderVO> result = new PageResult<>();
        result.setList(voList);
        result.setTotal(total);
        result.setPages(pages);
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);

        return result;
    }

    /**
     * 辅助方法：从 SecurityContextHolder 获取当前登录用户的 ID
     * 解决 "Cannot resolve method 'getLoginUserId'" 报错
     */
    /*private Long getLoginUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof JwtUser) {
            return ((JwtUser) principal).getId();
        }
        throw new RuntimeException("登录信息已过期，请重新登录");
    }*/



}