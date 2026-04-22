package com.example.takeouttry.DTO;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartItemVO {

    private Long id;

    private Long dishId;

    private String dishName;

    private String dishImage;

    private BigDecimal unitPrice;

    private Integer quantity;

    private Integer selected;

    private BigDecimal subtotal;
}