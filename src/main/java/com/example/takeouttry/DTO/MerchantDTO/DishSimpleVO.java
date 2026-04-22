package com.example.takeouttry.DTO.MerchantDTO;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DishSimpleVO {   // 内部类或单独文件
    private Long dishId;
    private String name;
    private BigDecimal price;
    private String image;
    private Integer sales;
}
