package com.example.takeouttry.controller;

import com.example.takeouttry.DTO.DishVO;
import com.example.takeouttry.DTO.PageResult;
import com.example.takeouttry.DTO.Result;
import com.example.takeouttry.entity.Dish;
import com.example.takeouttry.service.DishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/merchant/dishes")
public class DishController {

    @Autowired
    private DishService dishService;

    // 新增菜品
    @PostMapping
    public Result<String> addDish(@RequestBody Dish dish) {
        try {
            Long dishId = dishService.addDish(dish);
            return Result.success("新增成功，菜品ID：" + dishId);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // 更新菜品
    @PutMapping("/{id}")
    public Result<String> updateDish(@PathVariable Long id, @RequestBody Dish dish) {
        dish.setId(id);  // 确保 id 来自路径
        try {
            dishService.updateDish(dish);
            return Result.success("更新成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // 删除菜品
    @DeleteMapping("/{id}")
    public Result<String> deleteDish(@PathVariable Long id) {
        try {
            dishService.deleteDish(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // 查询自己店铺所有菜品
    @GetMapping
    public Result<List<Dish>> listMyDishes() {
        List<Dish> dishes = dishService.listMyDishes();
        return Result.success(dishes);
    }

    // 查询单条菜品（编辑前回显）
    @GetMapping("/{id}")
    public Result<Dish> getDish(@PathVariable Long id) {
        Dish dish = dishService.getDishById(id);
        if (dish == null) {
            return Result.error("菜品不存在", 404);
        }
        return Result.success(dish);
    }

    // 分页查询菜品（返回 DishVO，包含 categoryName）
    @GetMapping("/page")
    public Result<PageResult<DishVO>> pageMyDishes(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String name) {

        PageResult<DishVO> result = dishService.pageMyDishesWithCategory(pageNum, pageSize, name);
        return Result.success(result);
    }


    /**
     * 菜品图片上传
     * 路径：PUT /merchant/dishes/{id}/image
     */
    @PutMapping("/{id}/image")
    public Result<String> uploadDishImage(@PathVariable Long id,
                                          @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("请选择要上传的图片");
        }

        try {
            // 调用 service 层处理
            String imageUrl = dishService.uploadDishImage(id, file);
            return Result.success(imageUrl, "菜品图片上传成功");
        } catch (Exception e) {
            return Result.error("上传失败：" + e.getMessage());
        }
    }

}