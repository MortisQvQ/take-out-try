package com.example.takeouttry.mapper;

import com.example.takeouttry.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
* @author 33126
* @description 针对表【category(菜品分类表)】的数据库操作Mapper
* @createDate 2026-03-10 02:52:45
* @Entity .Category
*/
@Mapper
public interface CategoryMapper {

    int insert(Category record);

    int insertSelective(Category record);

    Category selectByPrimaryKey(Long id);

    List<Category> selectByMerchantId(Long merchantId);

    int updateByPrimaryKey(Category record);

    int updateByPrimaryKeySelective(Category record);

    int deleteByPrimaryKey(Long id);

    // 可选：删除商家所有分类（批量）
    // int deleteByMerchantId(Long merchantId);
    List<Category> selectByIds(@Param("ids")Collection<Long> ids);

}
