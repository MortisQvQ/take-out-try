package com.example.takeouttry.controller;

import com.example.takeouttry.DTO.Result;
import com.example.takeouttry.DTO.UserQuery;
import com.example.takeouttry.entity.User;
import com.example.takeouttry.security.JwtUser;
import com.example.takeouttry.service.AUserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/ausers")
public class AUserController {

    private final AUserService auserService;

    public AUserController(AUserService auserService) {
        this.auserService = auserService;
    }

    /**
     * 查看所有用户
     */
    @GetMapping("/list")
    public Result<List<User>> selectAllUsers() {
        List<User> users = auserService.listUsers();
        return Result.success(users);
    }

    /**
     * 根据id查询用户
     */
    @GetMapping("/{id}")
    public Result<User> getUserById(@PathVariable Long id) {
        User user = auserService.getUserById(id);
        if (user == null) {
            return Result.error("用户不存在", 404);
        }
        return Result.success(user);
    }

    /**
     * 条件查询用户
     */
    @GetMapping("/list2")
    public Result<List<User>> selectUsersByCondition(UserQuery query) {
        List<User> users = auserService.selectUsersByCondition(query);
        return Result.success(users);
    }

    /**
     * 新增用户（高权限）
     */
    @PostMapping("/add")
    public Result<User> addUser(@RequestBody User user) {
        User savedUser = auserService.addUser(user);
        return Result.success(savedUser, "新增用户成功");
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/update")
    public Result<User> updateUser(@RequestBody User user) {
        User updatedUser = auserService.updateUser(user);
        return Result.success(updatedUser, "更新用户成功");
    }

    /**
     * 根据用户id删除用户
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteUser(@PathVariable Long id) {
        auserService.deleteUser(id);
        return Result.success("删除成功");
    }

    /**
     * 用户头像上传
     */
    @PostMapping("/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file,
                                       @AuthenticationPrincipal JwtUser jwtUser) {
        if (jwtUser == null) {
            return Result.error("未登录", 401);
        }

        if (file.isEmpty()) {
            return Result.error("请选择要上传的文件");
        }

        try {
            String avatarUrl = auserService.uploadAvatar(jwtUser.getId(), file);   // 注意是 auserService
            return Result.success(avatarUrl, "头像上传成功");
        } catch (Exception e) {
            return Result.error("头像上传失败：" + e.getMessage());
        }
    }
}