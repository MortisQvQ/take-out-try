package com.example.takeouttry.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class DishVO {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String image;           // 对应 dish.image
    private Integer sales;
    private Boolean available;      // dish.status == 1
    private Long categoryId;
    private String categoryName;    // 从 category 表 join 出来
}
