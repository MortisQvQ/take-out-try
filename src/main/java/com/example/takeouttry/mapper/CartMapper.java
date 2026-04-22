package com.example.takeouttry.mapper;

import com.example.takeouttry.entity.Cart;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author Sherry
 * @description 针对表【cart(购物车表)】的数据库操作Mapper
 * @createDate 2026-xx-xx xx:xx:xx
 * @Entity com.example.takeouttry.entity.Cart
 */
@Mapper
public interface CartMapper {

    List<Cart> selectByUserId(Long userId);

    Cart selectById(Long id);

    Cart selectByUserMerchantDish(@Param("userId") Long userId,
                                  @Param("merchantId") Long merchantId,
                                  @Param("dishId") Long dishId);

    int insert(Cart cart);

    int updateById(Cart cart);

    int deleteById(Long id);

    int deleteByUserId(Long userId);

    int deleteByUserIdAndMerchantId(@Param("userId") Long userId,
                                    @Param("merchantId") Long merchantId);

    int existsByUserMerchantDish(@Param("userId") Long userId,
                                 @Param("merchantId") Long merchantId,
                                 @Param("dishId") Long dishId);

    int updateSelectedByUserAndMerchant(@Param("userId") Long userId,
                                        @Param("merchantId") Long merchantId,
                                        @Param("selected") Integer selected);

    Integer countSelectedByUserId(Long userId);

    List<Cart> selectByUserIdAndSelected(@Param("userId") Long userId, @Param("selected") Integer selected);

    int deleteByUserIdAndSelected(@Param("userId") Long userId, @Param("selected") Integer selected);

    // CartMapper
    List<Cart> selectByUserIdAndMerchantAndSelected(
            @Param("userId") Long userId,
            @Param("merchantId") Long merchantId,
            @Param("selected") Integer selected
    );

    int deleteByUserIdAndMerchantAndSelected(
            @Param("userId") Long userId,
            @Param("merchantId") Long merchantId,
            @Param("selected") Integer selected
    );

}