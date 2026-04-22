package com.example.takeouttry.security;

import com.example.takeouttry.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JTW 工具类 生成 解析 校验 token
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration:86400000}")  // yml 设置，毫秒ms  1s = 1000ms
    private long expiration;

    private SecretKey key;

    @PostConstruct
    public void init() {
        // 使用密钥生成 HMAC 签名 key
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }


    public String generateToken(User user) {
        return Jwts.builder()
                .subject(user.getUsername())           // 用户名作为 subject
                .claim("id", user.getId())                // 用户 ID
                .claim("role", user.getRole())            // 角色
                .issuedAt(new Date())                  // 签发时间
                .expiration(new Date(System.currentTimeMillis() + expiration))  // 过期时间
                .signWith(key)                            // 使用 HMAC 签名
                .compact();
    }

    /**
     * 解析并验证 token，返回 Claims
     * @throws JwtException 如果 token 无效、过期、签名错误等
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)           // 新版推荐写法（取代 setSigningKey）
                .build()
                .parseSignedClaims(token)  // 新版推荐（比 parseClaimsJws 更清晰）
                .getPayload();
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);  // 内部已验证签名、过期时间等
            return true;
        } catch (ExpiredJwtException e) {
            // 可以记录日志：log.info("Token 已过期: {}", token);
            return false;
        } catch (JwtException e) {
            // 签名错误、格式错误、nbf 未到等
            // log.warn("Token 验证失败: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            // 其他意外
            return false;
        }
    }


    public Long getUserIdFromToken(String token) {
        try{
            return parseToken(token).get("id", Long.class);
        }catch (JwtException e){
            return null;
        }
    }

    /**
     * 从 token 中获取角色
     */
    public Integer getRoleFromToken(String token) {
        try {
            return parseToken(token).get("role", Integer.class);
        } catch (JwtException e) {
            return null;
        }
    }

    /**
     * 从 token 中获取用户名（subject）
     */
    public String getUsernameFromToken(String token) {
        try {
            return parseToken(token).getSubject();
        } catch (JwtException e) {
            return null;
        }
    }


}
