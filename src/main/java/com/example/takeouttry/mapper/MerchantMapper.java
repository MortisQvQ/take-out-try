package com.example.takeouttry.mapper;

import com.example.takeouttry.entity.Merchant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * @description 商家表 merchant 的Mapper
 */
@Mapper
public interface MerchantMapper {

    int insert(Merchant merchant);

    Merchant selectById(@Param("id") Long id);

    Merchant selectByUserId(@Param("userId") Long userId);

    int updateById(Merchant merchant);

    int deleteById(@Param("id") Long id);

    List<Merchant> listMerchantsWithPage(@Param("keyword") String keyword,
                                         @Param("status") Integer status,
                                         @Param("offset") int offset,
                                         @Param("pageSize") int pageSize);

    // 新增：统计总数
    long countMerchants(@Param("keyword") String keyword,
                        @Param("status") Integer status);
}