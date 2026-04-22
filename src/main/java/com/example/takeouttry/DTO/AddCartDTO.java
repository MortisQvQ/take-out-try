package com.example.takeouttry.DTO;

import lombok.Data;


@Data
public class AddCartDTO {

    private Long dishId;

    private Integer quantity;
}