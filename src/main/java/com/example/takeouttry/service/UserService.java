package com.example.takeouttry.service;

import com.example.takeouttry.entity.User;
import com.example.takeouttry.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserMapper userMapper;

    public UserService(UserMapper auserMapper) {
        this.userMapper = auserMapper;
    }

    public List<User> listUsers() {
        return userMapper.findAll();
    }
}
