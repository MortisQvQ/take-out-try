package com.example.takeouttry.service;

import com.example.takeouttry.entity.Category;
import com.example.takeouttry.entity.Merchant;
import com.example.takeouttry.mapper.CategoryMapper;
import com.example.takeouttry.mapper.DishMapper;
import com.example.takeouttry.mapper.MerchantMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private MerchantMapper merchantMapper;

    @Autowired
    private AUserService aUserService;  // 用于从 username 查 userId
    @Autowired
    private DishMapper dishMapper;

    // 获取当前登录用户名
    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            return auth.getName();
        }
        throw new RuntimeException("未登录");
    }

    // 通过用户名 → userId → merchantId
    private Long getCurrentMerchantId() {
        String username = getCurrentUsername();
        Long userId = aUserService.getUserIdByUsername(username);
        if (userId == null) {
            throw new RuntimeException("用户不存在");
        }

        Merchant merchant = merchantMapper.selectByUserId(userId);
        if (merchant == null) {
            throw new RuntimeException("该用户不是商家");
        }
        return merchant.getId();
    }

    @Override
    @Transactional
    public Long addCategory(Category category) {
        Long merchantId = getCurrentMerchantId();

        // 必填校验
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("分类名称不能为空");
        }

        // 设置默认值
        category.setMerchantId(merchantId);
        category.setSort(category.getSort() != null ? category.getSort() : 0);  // 默认排序0
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());

        categoryMapper.insertSelective(category);
        return category.getId();
    }

    @Override
    @Transactional
    public void updateCategory(Category category) {
        if (category.getId() == null) {
            throw new IllegalArgumentException("分类ID不能为空");
        }

        Long merchantId = getCurrentMerchantId();

        // 权限校验
        /*Category existing = categoryMapper.selectByPrimaryKey(category.getId());
        if (existing == null || !existing.getMerchantId().equals(merchantId)) {
            throw new RuntimeException("分类不存在或无权限操作");
        }
        */
        category.setUpdateTime(LocalDateTime.now());
        categoryMapper.updateByPrimaryKeySelective(category);
    }

    @Override
    @Transactional
    public void deleteCategory(Long categoryId) {
        if (categoryId == null || categoryId <= 0) {
            throw new IllegalArgumentException("分类ID无效");
        }

        Long merchantId = getCurrentMerchantId();

        Category existing = categoryMapper.selectByPrimaryKey(categoryId);
        if (existing == null || !existing.getMerchantId().equals(merchantId)) {
            throw new RuntimeException("分类不存在或无权限删除");
        }

        int dishCount = dishMapper.countByCategoryId(categoryId);
        if (dishCount > 0) {
            throw new IllegalArgumentException("该分类下还有" + dishCount + "道美味,还不能删除，先清空试试吧!");
        }

        categoryMapper.deleteByPrimaryKey(categoryId);



    }

    @Override
    public List<Category> listMyCategories() {
        Long merchantId = getCurrentMerchantId();
        return categoryMapper.selectByMerchantId(merchantId);
    }

    @Override
    public Category getCategoryById(Long categoryId) {
        Long merchantId = getCurrentMerchantId();
        Category category = categoryMapper.selectByPrimaryKey(categoryId);
        if (category != null && category.getMerchantId().equals(merchantId)) {
            return category;
        }
        return null;
    }


    /*@Override
    public List<DishVO> listMerchant(Long merchantId,Long categoryId){
        Byte status = 1;

        List<Dish> dishes;
        if (categoryId != null && categoryId > 0) {
            dishes = dishMapper.selectByMerchantIdAndCategoryIdAndStatus(merchantId, categoryId, status);
        }else {
            dishes = dishMapper.selectByMerchantIdAndStatus(merchantId, status);
        }
        if (dishes == null || dishes.isEmpty()) {
            return Collections.emptyList();
        }

        // 批量获取分类名称
        Set<Long> categoryIds = dishes.stream()
                .map(Dish::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, String> categoryMap = new HashMap<>();
        if (!categoryIds.isEmpty()) {
            List<Category> categories = categoryMapper.selectByIds(categoryIds);
            for (Category c : categories) {
                categoryMap.put(c.getId(), c.getName());
            }
        }

        List<DishVO> vos = new ArrayList<>();
        for (Dish dish : dishes) {
            DishVO vo = new DishVO();
            vo.setId(dish.getId());
            vo.setName(dish.getName());
            vo.setDescription(dish.getDescription());
            vo.setPrice(dish.getPrice());
            vo.setImage(dish.getImage());
            vo.setSales(dish.getSales() != null ? dish.getSales() : 0);
            vo.setAvailable(true);  // 因为 status 已过滤
            vo.setCategoryId(dish.getCategoryId());
            vo.setCategoryName(categoryMap.getOrDefault(dish.getCategoryId(), "未分类"));
            vos.add(vo);
        }
        return vos;


    }*/

}