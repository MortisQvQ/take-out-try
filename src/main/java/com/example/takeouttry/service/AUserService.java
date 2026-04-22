package com.example.takeouttry.service;

import com.example.takeouttry.DTO.UserQuery;
import com.example.takeouttry.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AUserService extends UserDetailsService {

    List<User> listUsers();

    User getUserById(Long id);

    List<User> selectUsersByCondition(UserQuery query);

    User addUser(User user);

    User updateUser(User user);

    void deleteUser(Long id);

    Long getUserIdByUsername(String username);

    void register(User user);

    String login(String username, String rawPassword, String loginType);

    /**
     * 用户头像上传
     */
    String uploadAvatar(Long userId, MultipartFile file);
}