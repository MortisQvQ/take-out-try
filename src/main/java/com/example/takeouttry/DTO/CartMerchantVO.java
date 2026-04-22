package com.example.takeouttry.DTO;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CartMerchantVO {

    private Long merchantId;

    private String merchantName;

    private String merchantLogo;

    private List<CartItemVO> items;

    private BigDecimal merchantTotal;
}