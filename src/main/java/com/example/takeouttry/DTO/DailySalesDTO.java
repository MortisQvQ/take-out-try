package com.example.takeouttry.DTO;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DailySalesDTO {
    private String saleDate;
    private BigDecimal amount;
}