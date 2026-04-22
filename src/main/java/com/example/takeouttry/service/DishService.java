package com.example.takeouttry.service;

import com.example.takeouttry.DTO.DishVO;
import com.example.takeouttry.DTO.MerchantDTO.MerchantResponse;
import com.example.takeouttry.DTO.MerchantDTO.MerchantWithDishesVO;
import com.example.takeouttry.DTO.PageResult;
import com.example.takeouttry.entity.Dish;
import org.springframework.data.domain.Page;

import java.util.List;

public interface DishService {

    /**
     * 商家新增菜品
     * @param dish 菜品信息（前端传的）
     * @return 新增后的菜品ID
     */
    Long addDish(Dish dish);

    /**
     * 商家更新菜品（动态字段）
     * @param dish 更新后的菜品信息（包含id）
     */
    void updateDish(Dish dish);

    /**
     * 商家删除菜品
     * @param dishId 菜品ID
     */
    void deleteDish(Long dishId);

    /**
     * 商家查询自己店铺的所有菜品列表
     * @return 菜品列表（按创建时间降序）
     */
    List<Dish> listMyDishes();

    /**
     * 根据ID查询单条菜品（商家查看/编辑用）
     * @param dishId 菜品ID
     * @return Dish 对象（或null）
     */
    Dish getDishById(Long dishId);

    /**
     * 分页查询当前商家的菜品（支持名称模糊搜索）
     * @param pageNum 当前页（从1开始）
     * @param pageSize 每页条数
     * @param name 可选，菜品名称模糊搜索
     * @return 分页结果
     */
    PageResult<Dish> pageMyDishes(int pageNum, int pageSize, String name); //额。。虽然这个已经没用了 但是留着凑行数吧
    PageResult<DishVO> pageMyDishesWithCategory(int pageNum, int pageSize, String name);

    //List<DishVO> getDishesByMerchant(Long merchantId, Long CategoryId );

    // DishService.java 增加下面一行
    List<DishVO> listMerchantDishes(Long merchantId, Long categoryId);

    /**
     * 公开查询商家列表 + 每个商家推荐菜品（首页使用）
     * @param pageNum  页码，默认1
     * @param pageSize 每页数量，默认10
     * @param keyword  关键词（搜索商家名称或地址）
     * @param status   商家状态（1=营业中）
     * @return 分页结果
     */
    PageResult<MerchantWithDishesVO> listMerchantsWithDishes(Integer pageNum, Integer pageSize,
                                                             String keyword, Integer status);

    /**
     * 获取商家基本信息（公开）
     */
    MerchantResponse getMerchantInfo(Long merchantId);

}