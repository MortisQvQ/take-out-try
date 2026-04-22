package com.example.takeouttry.mapper;

import com.example.takeouttry.entity.Dish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author 33126
 * @description 针对表【dish(菜品表)】的数据库操作Mapper
 * @createDate 2026-03-10 02:06:43
 */
@Mapper
public interface DishMapper {

    int deleteByPrimaryKey(Long id);

    int insert(Dish record);

    int insertSelective(Dish record);

    Dish selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Dish record);

    int updateByPrimaryKey(Dish record);

    List<Dish> listByMerchantId(Long merchantId);

    // 分页 + 名称模糊查询（返回 List<Dish>）
    List<Dish> listByMerchantIdWithPage(@Param("merchantId") Long merchantId,
                                        @Param("name") String name,
                                        @Param("offset") int offset,
                                        @Param("pageSize") int pageSize);

    // 查询符合条件的总条数（用于计算总页数）
    long countByMerchantIdAndName(@Param("merchantId") Long merchantId,
                                  @Param("name") String name);

    Dish findByMerchantIdAndName(@Param("merchantId") Long merchantId,
                                 @Param("name") String name);

    int countByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * 根据商家ID和状态查询菜品列表
     */
    List<Dish> selectByMerchantIdAndStatus(
            @Param("merchantId") Long merchantId,
            @Param("status") Byte status
    );

    /**
     * 根据商家ID、分类ID和状态查询菜品列表
     */
    List<Dish> selectByMerchantIdAndCategoryIdAndStatus(
            @Param("merchantId") Long merchantId,
            @Param("categoryId") Long categoryId,
            @Param("status") Byte status
    );

    /**
     * 新增：查询商家推荐菜品（上架菜品，按销量降序，最多返回 limit 条）
     * 用于首页商家列表中每个商家展示几个推荐菜品
     *
     * @param merchantId 商家ID
     * @param limit      最多返回数量（建议4）
     * @return 推荐菜品列表
     */
    List<Dish> selectRecommendDishesByMerchantId(
            @Param("merchantId") Long merchantId,
            @Param("limit") int limit
    );
}