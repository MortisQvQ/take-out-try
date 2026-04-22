package com.example.takeouttry.DTO;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartSummaryDTO {

    private Integer totalQuantity;

    private BigDecimal totalAmount;
}