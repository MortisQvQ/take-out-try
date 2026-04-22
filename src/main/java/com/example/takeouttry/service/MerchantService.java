package com.example.takeouttry.service;

import com.example.takeouttry.DTO.MerchantDTO.RegisterMerchantRequest;
import com.example.takeouttry.DTO.MerchantDTO.MerchantResponse;
import com.example.takeouttry.entity.Merchant;
import org.springframework.web.multipart.MultipartFile;

public interface MerchantService {

    /**
     * 商家注册（创建用户 + 创建商家记录）
     */
    String registerMerchant(RegisterMerchantRequest request);

    /**
     * 根据用户ID查询商家信息
     */
    MerchantResponse getByUserId(Long userId);

    /**
     * 更新商家信息
     */
    boolean updateMerchant(Long userId, Merchant merchant);

    /**
     * 根据商家ID查询
     */
    Merchant getById(Long id);

    /**
     * 商家Logo上传
     */
    String uploadLogo(Long merchantId, MultipartFile file);


}