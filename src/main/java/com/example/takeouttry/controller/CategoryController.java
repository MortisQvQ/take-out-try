package com.example.takeouttry.controller;

import com.example.takeouttry.DTO.Result; // 确保导入你的 Result 类
import com.example.takeouttry.entity.Category;
import com.example.takeouttry.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
/// ?
///

@RestController
@RequestMapping("/merchant/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    // 新增分类
    @PostMapping
    public Result<Long> addCategory(@RequestBody Category category) {
        try {
            Long categoryId = categoryService.addCategory(category);
            return Result.success(categoryId, "新增分类成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // 更新分类
    @PutMapping("/{id}")
    public Result<String> updateCategory(@PathVariable Long id, @RequestBody Category category) {
        category.setId(id);
        try {
            categoryService.updateCategory(category);
            return Result.success(null, "更新成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // 删除分类
    @DeleteMapping("/{id}")
    public Result<String> deleteCategory(@PathVariable Long id) {
        try {
            categoryService.deleteCategory(id);
            return Result.success(null, "删除成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // 查询自己店铺的所有分类
    @GetMapping
    public Result<List<Category>> listMyCategories() {
        try {
            List<Category> categories = categoryService.listMyCategories();
            return Result.success(categories);
        } catch (Exception e) {
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    // 查询单条分类
    @GetMapping("/{id}")
    public Result<Category> getCategory(@PathVariable Long id) {
        Category category = categoryService.getCategoryById(id);
        if (category == null) {
            return Result.error("未找到该分类");
        }
        return Result.success(category);
    }
}