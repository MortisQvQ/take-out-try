package com.example.takeouttry.DTO.MerchantDTO;

import lombok.Data;

@Data
public class MerchantResponse {
    private Long id;
    private String name;
    private String address;
    private String phone;
    private String businessHours;
    private String logo;
    //private String description;
    private boolean status;
}