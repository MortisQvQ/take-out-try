package com.example.takeouttry.controller;

import com.example.takeouttry.DTO.DishVO;
import com.example.takeouttry.DTO.MerchantDTO.MerchantResponse;
import com.example.takeouttry.DTO.MerchantDTO.MerchantWithDishesVO;
import com.example.takeouttry.DTO.PageResult;
import com.example.takeouttry.DTO.Result;
import com.example.takeouttry.service.DishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dishes")
public class DishPublicController {

    @Autowired
    private DishService dishService;

    /**
     * 用户查看商家菜单（公开接口）
     * @param merchantId 商家ID
     * @param categoryId 可选，分类ID
     */
    @GetMapping("/merchant/{merchantId}")
    public Result<List<DishVO>> listMerchantDishes(
            @PathVariable Long merchantId,
            @RequestParam(required = false) Long categoryId) {

        if (merchantId == null || merchantId <= 0) {
            return Result.error("商家ID无效");
        }

        List<DishVO> dishes = dishService.listMerchantDishes(merchantId, categoryId);

        if (dishes.isEmpty()) {
            return Result.success(dishes, "该商家暂无在售菜品");
        }

        return Result.success(dishes);
    }

    /**
     * 新增：商家列表 + 每个商家展示推荐菜品（公开接口）
     * 用于首页展示多个商家和其热门菜品
     *
     * @param pageNum  页码，默认1
     * @param pageSize 每页数量，默认10
     * @param keyword  可选，商家名称或地址模糊搜索
     * @param status   可选，商家状态（默认1=营业中）
     */
    @GetMapping("/merchant/list")
    public Result<PageResult<MerchantWithDishesVO>> listMerchants(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer status) {

        // 参数防护
        if (pageNum == null || pageNum < 1) pageNum = 1;
        if (pageSize == null || pageSize < 1 || pageSize > 50) pageSize = 10;

        PageResult<MerchantWithDishesVO> pageResult =
                dishService.listMerchantsWithDishes(pageNum, pageSize, keyword, status);

        if (pageResult.getList() == null || pageResult.getList().isEmpty()) {
            return Result.success(pageResult, "暂无商家信息");
        }

        return Result.success(pageResult);
    }

    /**
     * 新增：获取商家基本信息（公开接口）
     * 用于商家详情页头部展示
     */
    @GetMapping("/merchant/info/{merchantId}")
    public Result<MerchantResponse> getMerchantInfo(@PathVariable Long merchantId) {
        if (merchantId == null || merchantId <= 0) {
            return Result.error("商家ID无效");
        }

        MerchantResponse info = dishService.getMerchantInfo(merchantId);
        if (info == null) {
            return Result.error("商家不存在或已下架");
        }

        return Result.success(info);
    }

    /**
     * 新增：获取单个菜品详细信息（公开接口）
     * 用于首页推荐位的图片和详情补全
     */
    @GetMapping("/{id}")
    public Result<DishVO> getDishDetail(@PathVariable Long id) {
        DishVO dish = dishService.getPublicDishById(id);
        return dish != null ? Result.success(dish) : Result.error("菜品不存在");
    }

}