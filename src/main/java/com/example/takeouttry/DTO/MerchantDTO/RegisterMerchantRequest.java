package com.example.takeouttry.DTO.MerchantDTO;

import lombok.Data;

@Data
public class RegisterMerchantRequest {
    private String username;
    private String password;
    private String phone;
    private String shopName;
    private String shopAddress;
    private String shopPhone;
}