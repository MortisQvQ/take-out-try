package com.example.takeouttry.controller;

import com.example.takeouttry.entity.User;
import com.example.takeouttry.security.JwtUser;
import com.example.takeouttry.service.AUserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class TestController {

    private final AUserService aUserService;

    // 构造器注入
    public TestController(AUserService aUserService) {
        this.aUserService = aUserService;
    }

    @GetMapping("/test")
    public String test(@AuthenticationPrincipal String username) {
        if (username == null) {
            return "未登录或 token 无效";
        }
        return "你已登录，当前用户：" + username;
    }

    /**
     * whoami 接口 - 返回用户完整信息（包含头像）
     */
    @GetMapping("/whoami")
    public Map<String, Object> whoAmI(Authentication authentication) {
        Map<String, Object> info = new HashMap<>();

        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            info.put("username", username);
            info.put("authorities", authentication.getAuthorities());
            info.put("details", authentication.getDetails());

            // ==================== 关键：查询完整用户信息（包含 avatarUrl） ====================
            if (authentication.getPrincipal() instanceof JwtUser jwtUser) {
                Long userId = jwtUser.getId();
                User user = aUserService.getUserById(userId);
                if (user != null) {
                    info.put("username", user.getUsername());
                    info.put("phone", user.getPhone());
                    info.put("avatarUrl", user.getAvatarUrl());   // 返回头像路径
                    info.put("role", user.getRole());
                }
            }
        } else {
            info.put("message", "未登录");
        }

        return info;
    }
}