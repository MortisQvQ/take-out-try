package com.example.takeouttry.controller;

import com.example.takeouttry.DTO.LoginResponse;
import com.example.takeouttry.DTO.MerchantDTO.MerchantResponse;
import com.example.takeouttry.DTO.MerchantDTO.RegisterMerchantRequest;
import com.example.takeouttry.DTO.Result; // 确保导入 Result
import com.example.takeouttry.service.AUserService;
import com.example.takeouttry.service.MerchantService;
import com.example.takeouttry.entity.Merchant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/merchant")
public class MerchantController {

    @Autowired
    private MerchantService merchantService;

    @Autowired
    private AUserService aUserService;

    /**
     * 商家注册
     */
    @PostMapping("/register")
    public Result<?> register(@RequestBody RegisterMerchantRequest request){
        try {
            String token = merchantService.registerMerchant(request);
            // 包装进 Result，这样前端拦截器 res.code === 200 就能通过了
            return Result.success(new LoginResponse(token), "商家注册成功");
        } catch (Exception e){
            return Result.error("注册失败：" + e.getMessage());
        }
    }

    /**
     * 获取商家资料
     */
    @GetMapping("/profile")
    public Result<?> getProfile(Authentication authentication){
        if(authentication == null || !authentication.isAuthenticated()){
            return Result.error("未登录", 401);
        }

        String username = authentication.getName();
        long userId = aUserService.getUserIdByUsername(username);
        MerchantResponse response = merchantService.getByUserId(userId);

        if (response == null) {
            return Result.error("未找到商家信息", 404);
        }
        return Result.success(response);
    }

    /**
     * 更新商家信息（需登录）
     */
    @PutMapping("/profile")
    public Result<String> updateProfile(@RequestBody Merchant merchant, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return Result.error("未登录", 401);
        }

        String username = authentication.getName();
        Long userId = aUserService.getUserIdByUsername(username);

        boolean success = merchantService.updateMerchant(userId, merchant);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }


    /**
     * 商家Logo上传
     */
    @PostMapping("/logo")
    public Result<String> uploadLogo(@RequestParam("file") MultipartFile file,
                                     @RequestParam("merchantId") Long merchantId) {
        // 基础校验
        if (merchantId == null) {
            return Result.error("商家ID不能为空");
        }
        if (file.isEmpty()) {
            return Result.error("请选择要上传的文件");
        }

        try {
            // 调用商家服务的上传方法
            String logoUrl = merchantService.uploadLogo(merchantId, file);
            return Result.success(logoUrl, "Logo上传成功");
        } catch (Exception e) {
            return Result.error("Logo上传失败：" + e.getMessage());
        }
    }
}