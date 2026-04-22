package com.example.takeouttry.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor  // lombok 自动生成构造器注入
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        log.debug("JwtAuthenticationFilter 开始处理请求: {}", request.getRequestURI());

        String header = request.getHeader("Authorization");
        log.debug("Authorization Header: {}", header);

        if (header == null || !header.startsWith("Bearer ")) {
            log.debug("无有效 Bearer Token，直接放行");
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7).trim();
        if (token.isEmpty()) {
            log.warn("Token 为空字符串，忽略");
            filterChain.doFilter(request, response);
            return;
        }

        log.debug("尝试解析 token: {}...", token.substring(0, Math.min(20, token.length())));

        try {
            Claims claims = jwtUtil.parseToken(token);
            String username = claims.getSubject();
            Long userId = claims.get("id", Long.class);
            Integer role = claims.get("role", Integer.class);

            log.info("Token 解析成功:userId{}, username={}, role={}",userId, username, role);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                JwtUser jwtUser = new JwtUser(userId, username, role);
                String roleName = switch (role != null ? role : 1) {
                    case 2 -> "ROLE_ADMIN";
                    // case 3 -> "ROLE_DELIVERY";
                    default -> "ROLE_USER";
                };

                var authorities = List.of(new SimpleGrantedAuthority(roleName));

                var authentication = new UsernamePasswordAuthenticationToken(
                        jwtUser, null, jwtUser.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("已设置 SecurityContext: 用户 {} 已认证", username);
            }

        } catch (ExpiredJwtException e) {
            log.warn("Token 已过期: {}", e.getMessage());
            // 直接设置 401 + 友好提示
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"token 已过期，请重新登录\"}");
            return;  // 直接结束，不再放行

        } catch (JwtException e) {
            log.warn("Token 验证失败（非过期原因）: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"无效的 token\"}");
            return;

        } catch (Exception e) {
            log.error("Token 处理异常", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":500,\"message\":\"服务器内部错误\"}");
            return;
        }

        filterChain.doFilter(request, response);
        System.out.println("JwtAuthenticationFilter 执行了");
    }

    /*
     *  判断bean是否创建？检查检查检查
     */
    /*
    @PostConstruct
    public void init() {
        System.out.println("====================");
        System.out.println("【关键诊断】JwtAuthenticationFilter Bean 已创建！");
        System.out.println("当前时间: " + new java.util.Date());
        System.out.println("====================");
    }*/
}