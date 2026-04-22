package com.example.takeouttry.service;

import com.example.takeouttry.DTO.DishVO;
import com.example.takeouttry.DTO.MerchantDTO.DishSimpleVO;
import com.example.takeouttry.DTO.MerchantDTO.MerchantResponse;
import com.example.takeouttry.DTO.MerchantDTO.MerchantWithDishesVO;
import com.example.takeouttry.DTO.PageResult;
import com.example.takeouttry.entity.Category;
import com.example.takeouttry.entity.Dish;
import com.example.takeouttry.entity.Merchant;
import com.example.takeouttry.mapper.CategoryMapper;
import com.example.takeouttry.mapper.DishMapper;
import com.example.takeouttry.mapper.MerchantMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private MerchantMapper merchantMapper;  // 用于从 userId 查 merchantId

    @Autowired
    private AUserService aUserService;

    @Autowired
    private CategoryMapper categoryMapper;



    // 从 SecurityContext 获取当前登录用户名
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        throw new RuntimeException("未登录");
    }

    // 从用户名查 userId，再查 merchantId（你的业务中 merchant 表有 user_id）
    private Long getCurrentMerchantId() {
        String username = getCurrentUsername();
        // 假设你有 AUserService.getUserIdByUsername 方法
        Long userId = aUserService.getUserIdByUsername(username);  // 你需要注入 AUserService
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
    public Long addDish(Dish dish) {
        Long merchantId = getCurrentMerchantId();

        // 校验必填字段（可以加 @Valid，但这里手动写）
        if (dish.getName() == null || dish.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("菜品名称不能为空");
        }
        if (dish.getPrice() == null || dish.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("价格必须大于0");
        }



        // 新增：检查同一商家下是否已存在同名菜品（忽略大小写和空格）
        String normalizedName = dish.getName().trim().toLowerCase();
        Dish existing = dishMapper.findByMerchantIdAndName(merchantId, normalizedName);
        if (existing != null) {
            throw new IllegalArgumentException("该商家已存在同名菜品：" + dish.getName());
        }
        if (dish.getCategoryId() != null) {
            Category category = categoryMapper.selectByPrimaryKey(dish.getCategoryId());
            //允许分类为null
            if (category == null) {
                throw new IllegalArgumentException("这个分类不存在哦");
            }
            if(!category.getMerchantId().equals(merchantId)) {
                throw new IllegalArgumentException("这个分类不是你的！也许也不存在，去添加一个吧");
            }
        }

        // 设置默认值
        dish.setMerchantId(merchantId);
        dish.setStatus(1);          // 默认在售
        dish.setSales(0);           // 默认销量0
        dish.setCreateTime(LocalDateTime.now());
        dish.setUpdateTime(LocalDateTime.now());

        dishMapper.insertSelective(dish);  // 用动态插入，安全
        return dish.getId();  //返回自增ID(useGeneratedKeys 保证 dish.id 回填)
    }

    /**
     * 更新菜品信息
     */
    @Override
    @Transactional
    public void updateDish(Dish dish) {
        if (dish.getId() == null) {
            throw new IllegalArgumentException("菜品ID不能为空");
        }

        Long merchantId = getCurrentMerchantId();

        // 1. 查询现有菜品，验证权限
        Dish existing = dishMapper.selectByPrimaryKey(dish.getId());
        if (existing == null || !existing.getMerchantId().equals(merchantId)) {
            throw new RuntimeException("菜品不存在或无权限操作");
        }

        // 2. 更新允许修改的字段（安全拷贝）
        if (dish.getName() != null && !dish.getName().trim().isEmpty()) {
            existing.setName(dish.getName().trim());
        }
        if (dish.getDescription() != null) {
            existing.setDescription(dish.getDescription());
        }
        if (dish.getPrice() != null && dish.getPrice().compareTo(BigDecimal.ZERO) > 0) {
            existing.setPrice(dish.getPrice());
        }
        if (dish.getImage() != null) {
            existing.setImage(dish.getImage());
        }
        if (dish.getStatus() != null) {
            existing.setStatus(dish.getStatus());
        }

        // 3. 修改分类（允许修改，且做严格校验）
        if (dish.getCategoryId() != null) {
            if (dish.getCategoryId() == 0) {
                // 如果你允许设置为“未分类”，可以在这里处理
                existing.setCategoryId(0L);
            } else {
                Category category = categoryMapper.selectByPrimaryKey(dish.getCategoryId());
                if (category == null) {
                    throw new IllegalArgumentException("选择的分类不存在");
                }
                if (!category.getMerchantId().equals(merchantId)) {
                    throw new IllegalArgumentException("该分类不属于你的店铺！");
                }
                existing.setCategoryId(dish.getCategoryId());
            }
        }
        // 禁止修改的字段（不复制）
        // - merchantId（永不改）
        // - sales（销量只能系统累加，不能手动改）
        // - createTime（创建时间永不改）
        // - id（主键不改）

        // 4. 更新时间
        existing.setUpdateTime(LocalDateTime.now());

        // 5. 执行更新（使用 existing，保证安全）
        dishMapper.updateByPrimaryKeySelective(existing);
    }

    @Override
    @Transactional
    public void deleteDish(Long dishId) {
        if (dishId == null || dishId <= 0) {
            throw new IllegalArgumentException("菜品ID无效");
        }

        Long merchantId = getCurrentMerchantId();

        Dish existing = dishMapper.selectByPrimaryKey(dishId);
        if (existing == null || !existing.getMerchantId().equals(merchantId)) {
            throw new RuntimeException("菜品不存在或无权限删除");
        }

        dishMapper.deleteByPrimaryKey(dishId);
        // 后续可以加逻辑：如果有订单关联，可以软删除或提示
    }

    @Override
    public List<Dish> listMyDishes() {
        Long merchantId = getCurrentMerchantId();
        return dishMapper.listByMerchantId(merchantId);
    }

    @Override
    public Dish getDishById(Long dishId) {
        Long merchantId = getCurrentMerchantId();
        Dish dish = dishMapper.selectByPrimaryKey(dishId);
        if (dish != null && dish.getMerchantId().equals(merchantId)) {
            return dish;
        }
        return null;  // 或抛异常
    }

    //展示
    @Override
    public PageResult<Dish> pageMyDishes(int pageNum, int pageSize, String name) {
        Long merchantId = getCurrentMerchantId();

        // 参数防护
        if (pageNum < 1) pageNum = 1;
        if (pageSize < 1) pageSize = 10;
        if (name != null && name.trim().isEmpty()) name = null;

        int offset = (pageNum - 1) * pageSize;

        List<Dish> list = dishMapper.listByMerchantIdWithPage(merchantId, name, offset, pageSize);
        long total = dishMapper.countByMerchantIdAndName(merchantId, name);

        int pages = (int) Math.ceil((double) total / pageSize);

        PageResult<Dish> result = new PageResult<>();
        result.setList(list);
        result.setTotal(total);
        result.setPages(pages);
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);

        return result;
    }

    @Override
    public List<DishVO> listMerchantDishes(Long merchantId, Long categoryId) {
        if (merchantId == null || merchantId <= 0) {
            return Collections.emptyList();
        }

        Byte status = 1; // 只查在售

        List<Dish> dishes;
        if (categoryId != null && categoryId > 0) {
            dishes = dishMapper.selectByMerchantIdAndCategoryIdAndStatus(merchantId, categoryId, status);
        } else {
            dishes = dishMapper.selectByMerchantIdAndStatus(merchantId, status);
        }

        if (dishes == null || dishes.isEmpty()) {
            return Collections.emptyList();
        }

        // 构建分类名称映射（使用已有的 selectByMerchantId 方法）
        List<Category> categories = categoryMapper.selectByMerchantId(merchantId);
        Map<Long, String> categoryNameMap = categories.stream()
                .collect(Collectors.toMap(
                        Category::getId,
                        Category::getName,
                        (old, newV) -> old
                ));

        // 转换为 VO
        List<DishVO> result = new ArrayList<>();
        for (Dish dish : dishes) {
            DishVO vo = new DishVO();
            vo.setId(dish.getId());
            vo.setName(dish.getName());
            vo.setDescription(dish.getDescription());
            vo.setPrice(dish.getPrice());
            vo.setImage(dish.getImage());
            vo.setSales(dish.getSales() != null ? dish.getSales() : 0);
            vo.setAvailable(true);
            vo.setCategoryId(dish.getCategoryId());
            vo.setCategoryName(categoryNameMap.getOrDefault(dish.getCategoryId(), "未分类"));
            result.add(vo);
        }

        return result;
    }

    @Override
    public PageResult<DishVO> pageMyDishesWithCategory(int pageNum, int pageSize, String name) {
        Long merchantId = getCurrentMerchantId();

        if (pageNum < 1) pageNum = 1;
        if (pageSize < 1) pageSize = 10;
        if (name != null && name.trim().isEmpty()) name = null;

        int offset = (pageNum - 1) * pageSize;

        // 调用 Mapper 分页查询
        List<Dish> list = dishMapper.listByMerchantIdWithPage(merchantId, name, offset, pageSize);
        long total = dishMapper.countByMerchantIdAndName(merchantId, name);

        int pages = (int) Math.ceil((double) total / pageSize);

        // 转换为 DishVO 并填充分类名称
        List<DishVO> voList = list.stream().map(dish -> {
            DishVO vo = new DishVO();
            vo.setId(dish.getId());
            vo.setName(dish.getName());
            vo.setDescription(dish.getDescription());
            vo.setPrice(dish.getPrice());
            vo.setImage(dish.getImage());
            vo.setSales(dish.getSales() != null ? dish.getSales() : 0);
            vo.setAvailable(dish.getStatus() != null && dish.getStatus() == 1);
            vo.setCategoryId(dish.getCategoryId());

            // 填充分类名称
            if (dish.getCategoryId() != null) {
                Category category = categoryMapper.selectByPrimaryKey(dish.getCategoryId());
                vo.setCategoryName(category != null ? category.getName() : "未分类");
            } else {
                vo.setCategoryName("未分类");
            }

            return vo;
        }).collect(Collectors.toList());

        PageResult<DishVO> result = new PageResult<>();
        result.setList(voList);
        result.setTotal(total);
        result.setPages(pages);
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);

        return result;
    }

    /**
     * 新增方法：公开查询商家列表 + 每个商家推荐菜品（最多4个上架菜品，按销量排序）
     */
    @Override
    public PageResult<MerchantWithDishesVO> listMerchantsWithDishes(Integer pageNum, Integer pageSize, String keyword, Integer status) {
        if (pageNum == null || pageNum < 1) pageNum = 1;
        if (pageSize == null || pageSize < 1) pageSize = 10;
        if (pageSize > 50) pageSize = 50;
        if (keyword != null && keyword.trim().isEmpty()) keyword = null;

        int offset = (pageNum - 1) * pageSize;

        // 修改这里：暂时不根据 status 过滤，因为你的商家 status 默认都是 0
        // 我们把 status = 0 和 1 都视为有效营业状态
        List<Merchant> merchantList = merchantMapper.listMerchantsWithPage(keyword, null, offset, pageSize);
        long total = merchantMapper.countMerchants(keyword, null);

        int pages = (int) Math.ceil((double) total / pageSize);

        List<MerchantWithDishesVO> voList = merchantList.stream().map(merchant -> {
            MerchantWithDishesVO vo = new MerchantWithDishesVO();
            vo.setMerchantId(merchant.getId());
            vo.setName(merchant.getName());
            vo.setAddress(merchant.getAddress());
            vo.setPhone(merchant.getPhone());
            vo.setLogo(merchant.getLogo());
            vo.setStatus(merchant.getStatus());
            vo.setBusinessHours(merchant.getBusinessHours());

            // 查询推荐菜品（最多4个上架菜品）
            List<Dish> recommendDishes = dishMapper.selectRecommendDishesByMerchantId(merchant.getId(), 4);

            List<DishSimpleVO> dishSimpleList = recommendDishes.stream().map(dish -> {
                DishSimpleVO simple = new DishSimpleVO();
                simple.setDishId(dish.getId());
                simple.setName(dish.getName());
                simple.setPrice(dish.getPrice());
                simple.setImage(dish.getImage());
                simple.setSales(dish.getSales() != null ? dish.getSales() : 0);
                return simple;
            }).collect(Collectors.toList());

            vo.setDishes(dishSimpleList);
            return vo;
        }).collect(Collectors.toList());

        PageResult<MerchantWithDishesVO> result = new PageResult<>();
        result.setList(voList);
        result.setTotal(total);
        result.setPages(pages);
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);

        return result;
    }


    @Override
    public MerchantResponse getMerchantInfo(Long merchantId) {
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            return null;
        }

        MerchantResponse response = new MerchantResponse();
        response.setId(merchant.getId());
        response.setName(merchant.getName());
        response.setAddress(merchant.getAddress());
        response.setPhone(merchant.getPhone());
        response.setBusinessHours(merchant.getBusinessHours());
        response.setLogo(merchant.getLogo());
        response.setStatus(merchant.getStatus() == 1);   // boolean 类型

        return response;
    }
}