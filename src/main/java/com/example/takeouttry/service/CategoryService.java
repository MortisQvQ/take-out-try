package com.example.takeouttry.service;

import com.example.takeouttry.entity.Category;
import java.util.List;

public interface CategoryService {

    /**
     * 商家新增分类
     * @param category 分类信息
     * @return 新增后的分类ID
     */
    Long addCategory(Category category);

    /**
     * 商家更新分类（动态字段）
     * @param category 更新后的分类信息（必须包含id）
     */
    void updateCategory(Category category);

    /**
     * 商家删除分类
     * @param categoryId 分类ID
     */
    void deleteCategory(Long categoryId);

    /**
     * 商家查询自己店铺的所有分类列表
     * @return 分类列表（按 sort 升序 + 创建时间降序）
     */
    List<Category> listMyCategories();

    /**
     * 根据ID查询单条分类（编辑前回显用）
     * @param categoryId 分类ID
     * @return Category 对象（或null）
     */
    Category getCategoryById(Long categoryId);
}