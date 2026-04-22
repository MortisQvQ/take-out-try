package com.example.takeouttry.security;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * JWT 用户信息载体（实现 UserDetails）
 */
@Data
public class JwtUser implements UserDetails {

    private final Long id;
    private final String username;
    private final Integer role;
    private final Collection<? extends GrantedAuthority> authorities;

    /**
     * 构造方法：根据 user 信息创建 JwtUser
     */
    public JwtUser(Long id, String username, Integer role) {
        this.id = id;
        this.username = username;
        this.role = role;

        // 根据 role 生成权限
        String roleName = switch (role != null ? role : 1) {
            case 2 -> "ROLE_ADMIN";
            default -> "ROLE_USER";
        };

        this.authorities = List.of(new SimpleGrantedAuthority(roleName));
    }

    // UserDetails 接口方法（密码不需要，JWT 认证不依赖密码）
    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    // 如果你需要自定义 toString 或其他方法，也可以保留
    // 但 @Data 已经自动生成了 toString、equals、hashCode
}