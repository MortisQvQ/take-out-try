package com.example.takeouttry.controller;

import com.example.takeouttry.DTO.LoginRequest;
import com.example.takeouttry.DTO.LoginResponse;
import com.example.takeouttry.DTO.RegisterRequest;
import com.example.takeouttry.DTO.Result;
import com.example.takeouttry.entity.User;
import com.example.takeouttry.service.AUserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AUserService aUserService;

    public AuthController(AUserService aUserService) {
        this.aUserService = aUserService;
    }

    /**
     * 注册功能
     */
    @PostMapping("/register")
    public Result<String> register(@RequestBody RegisterRequest request) {
        // 简单校验
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            return Result.error("用户名不能为空");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            return Result.error("密码不能为空");
        }
        if (request.getPhone() == null || request.getPhone().trim().isEmpty()) {
            return Result.error("手机号不能为空");
        }

        try {
            User user = new User();
            user.setUsername(request.getUsername());
            user.setPassword(request.getPassword());
            user.setPhone(request.getPhone());
            user.setRole(1); // 默认注册为普通用户

            aUserService.register(user);
            return Result.success("注册成功，请登录");
        } catch (IllegalArgumentException e) {
            // 捕获注册过程中的业务校验错误（如用户名已存在）
            return Result.error(e.getMessage());
        } catch (Exception e) {
            // 记录异常，防止敏感报错暴露给前端
            e.printStackTrace();
            return Result.error("注册失败，请稍后再试");
        }
    }

    /**
     * 登录功能
     */
    @PostMapping("/login")
    public Result<?> login(@RequestBody LoginRequest request){
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            return Result.error("用户名不能为空");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            return Result.error("密码不能为空");
        }
        // 这里记得补充对 type 的非空校验
        /*if (request.getType() == null || request.getType().isBlank()) {
            return Result.error("登录类型不能为空");
        }*/

        try {
            String token = aUserService.login(request.getUsername(), request.getPassword(), request.getType());
            return Result.success(new LoginResponse(token), "登录成功");
        } catch (IllegalArgumentException e) {
            // 明确返回 401，符合 RESTful 规范
            return Result.error(e.getMessage(), 401);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("服务器内部错误", 500);
        }
    }

}