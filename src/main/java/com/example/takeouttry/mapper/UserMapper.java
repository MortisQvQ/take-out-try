package com.example.takeouttry.mapper;


import com.example.takeouttry.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
@Mapper
public interface UserMapper {

    @Select("select * from `a_users`")
    List<User> findAll();
}