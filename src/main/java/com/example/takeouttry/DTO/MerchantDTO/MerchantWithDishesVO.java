package com.example.takeouttry.DTO.MerchantDTO;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class MerchantWithDishesVO {
    private Long merchantId;
    private String name;
    private String address;
    private String phone;
    private String logo;
    private Integer status;
    private String businessHours;
    private Integer sales;          // 店铺总销量（可选）
    private Double distance;        // 距离（公里），前端可自行计算或后端根据经纬度算

    private List<DishSimpleVO> dishes;  // 推荐菜品（最多4个）
}

