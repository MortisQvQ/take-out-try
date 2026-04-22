package com.example.takeouttry.mapper;

import com.example.takeouttry.entity.User;
import org.apache.ibatis.annotations.Mapper;
import com.example.takeouttry.DTO.UserQuery;


import java.util.List;

/**
* @author 33126
* @description 针对表【a_users(用户表)】的数据库操作Mapper
* @createDate 2026-01-29 19:59:57
* @Entity com.example.takeouttry.entity.User
*/
@Mapper
public interface AUsersMapper {

    List <User> selectAllUsers();
    User selectUserById(Long id);
    List <User> selectUsersByCondition(UserQuery query);
    int addUser(User user);
    int updateUser(User user);
    int deleteUser(Long id);

    /* username 查找用户是否已经存在？ */
    int existsByUsername(String name);
    //int deleteUser(@Param("id") Long id, @Param("operatorId") Long operatorId);   登陆安全spring security + *** 预留

    int existsByPhone(String Phone);

    User findByUsername(String username);
    Long findIdByUsername(String username);

}
