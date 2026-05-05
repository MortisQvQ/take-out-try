package com.example.takeouttry.service;

import com.example.takeouttry.DTO.*;
import com.example.takeouttry.entity.Cart;
import com.example.takeouttry.entity.Dish;
import com.example.takeouttry.entity.Merchant;
import com.example.takeouttry.mapper.CartMapper;
import com.example.takeouttry.mapper.DishMapper;
import com.example.takeouttry.mapper.MerchantMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 购物车服务实现类
 */
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private MerchantMapper merchantMapper;

    /**
     *
     * @param userId 当前登录用户ID（从 SecurityContext 或 token 获取）
     * @param dto 加购参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addToCart(Long userId, AddCartDTO dto) {
        if (dto.getDishId() == null) {
            throw new IllegalArgumentException("菜品ID不能为空");
        }
        Integer addQuantity = (dto.getQuantity() != null && dto.getQuantity() > 0)
                ? dto.getQuantity() : 1;

        // 1. 查询菜品，确保存在且在售
        Dish dish = dishMapper.selectByPrimaryKey(dto.getDishId());
        if (dish == null) {
            throw new IllegalArgumentException("菜品不存在");
        }
        if (dish.getStatus() != 1) {
            throw new IllegalArgumentException("菜品已下架或售罄");
        }

        Long merchantId = dish.getMerchantId();

        // 2. 检查该用户-商家-菜品组合是否已存在
        Cart existing = cartMapper.selectByUserMerchantDish(userId, merchantId, dto.getDishId());

        if (existing != null) {
            // 存在 → 累加数量
            existing.setQuantity(existing.getQuantity() + addQuantity);  //这里更新数量不更新价格！如果存在按照第一次价格快照
            cartMapper.updateById(existing);
        } else {
            // 不存在 → 新增一条
            Cart newCart = new Cart();
            newCart.setUserId(userId);
            newCart.setMerchantId(merchantId);
            newCart.setDishId(dto.getDishId());
            newCart.setQuantity(addQuantity);
            newCart.setSelected(1);           // 默认选中
            newCart.setUnitPrice(dish.getPrice());
            newCart.setDishName(dish.getName());
            newCart.setDishImage(dish.getImage());
            cartMapper.insert(newCart);
        }
    }

    /**
     *
     * @param userId 用户ID（用于权限校验）
     * @param cartId 购物车项ID
     * @param dto 更新参数（quantity 和 selected 可选，部分更新）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCartItem(Long userId, Long cartId, UpdateCartDTO dto) {
        if (cartId == null) {
            throw new IllegalArgumentException("购物车项ID不能为空");
        }

        Cart cart = cartMapper.selectById(cartId);
        if (cart == null) {
            throw new IllegalArgumentException("购物车项不存在");
        }
        if (!cart.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权操作此购物车项");
        }

        boolean changed = false;

        if (dto.getQuantity() != null) {
            if (dto.getQuantity() <= 0) {
                throw new IllegalArgumentException("数量必须大于0");
            }
            cart.setQuantity(dto.getQuantity());
            changed = true;
        }

        if (dto.getSelected() != null) {
            if (dto.getSelected() != 0 && dto.getSelected() != 1) {
                throw new IllegalArgumentException("选中状态只能为0或1");
            }
            cart.setSelected(dto.getSelected());
            changed = true;
        }

        if (changed) {
            cartMapper.updateById(cart);
        }
    }

    /**
     *
     * @param userId 用户ID
     * @param cartId 购物车项ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeFromCart(Long userId, Long cartId) {
        if (cartId == null) {
            throw new IllegalArgumentException("购物车项ID不能为空");
        }

        Cart cart = cartMapper.selectById(cartId);
        if (cart == null || !cart.getUserId().equals(userId)) {
            throw new IllegalArgumentException("购物车项不存在或无权删除");
        }

        cartMapper.deleteById(cartId);
    }

    /**
     *
     * @param userId 用户ID
     * @param merchantId 商家ID（可选）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearCart(Long userId, Long merchantId) {
        if (merchantId != null && merchantId > 0) {
            cartMapper.deleteByUserIdAndMerchantId(userId, merchantId);
        } else {
            cartMapper.deleteByUserId(userId);
        }
    }

    @Override
    public List<CartMerchantVO> listCartGrouped(Long userId) {
        List<Cart> carts = cartMapper.selectByUserId(userId);
        if (carts == null || carts.isEmpty()) {
            return Collections.emptyList();
        }

        // 按商家分组
        Map<Long, List<Cart>> groupedByMerchant = carts.stream()
                .collect(Collectors.groupingBy(Cart::getMerchantId));

        List<CartMerchantVO> result = new ArrayList<>();

        for (Map.Entry<Long, List<Cart>> entry : groupedByMerchant.entrySet()) {
            Long merchantId = entry.getKey();
            List<Cart> itemList = entry.getValue();

            Merchant merchant = merchantMapper.selectById(merchantId);
            if (merchant == null) {
                continue; // 商家不存在，跳过
            }

            CartMerchantVO vo = new CartMerchantVO();
            vo.setMerchantId(merchantId);
            vo.setMerchantName(merchant.getName());
            vo.setMerchantLogo(merchant.getLogo());

            // 转换为 VO
            List<CartItemVO> items = itemList.stream().map(cart -> {
                CartItemVO item = new CartItemVO();
                item.setId(cart.getId());
                item.setDishId(cart.getDishId());
                item.setDishName(cart.getDishName());
                item.setDishImage(cart.getDishImage());
                item.setUnitPrice(cart.getUnitPrice());
                item.setQuantity(cart.getQuantity());
                item.setSelected(cart.getSelected());

                // 小计 = 单价 × 数量
                BigDecimal subtotal = cart.getUnitPrice()
                        .multiply(BigDecimal.valueOf(cart.getQuantity()));
                item.setSubtotal(subtotal);

                return item;
            }).collect(Collectors.toList());

            vo.setItems(items);

            // 该商家选中商品小计
            BigDecimal merchantTotal = items.stream()
                    .filter(it -> it.getSelected() == 1)
                    .map(CartItemVO::getSubtotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            vo.setMerchantTotal(merchantTotal);

            result.add(vo);
        }



        return result;
    }

    @Override
    public CartSummaryDTO getCartSummary(Long userId) {
        // 选中商品的总数量（SUM(quantity) where selected=1）
        Integer totalQuantity = cartMapper.countSelectedByUserId(userId);
        if (totalQuantity == null) {
            totalQuantity = 0;
        }

        // 选中商品的总金额
        List<Cart> carts = cartMapper.selectByUserId(userId);
        BigDecimal totalAmount = carts.stream()
                .filter(c -> c.getSelected() == 1)
                .map(c -> c.getUnitPrice().multiply(BigDecimal.valueOf(c.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        CartSummaryDTO summary = new CartSummaryDTO();
        summary.setTotalQuantity(totalQuantity);
        summary.setTotalAmount(totalAmount);

        return summary;
    }

    @Override
    public Integer getCartTotalCount(Long userId) {
        if (userId == null) {
            return 0;
        }

        Integer totalCount = cartMapper.sumQuantityByUserId(userId);
        return totalCount != null ? totalCount : 0;


    }
}