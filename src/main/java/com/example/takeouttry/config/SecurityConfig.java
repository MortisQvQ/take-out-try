package com.example.takeouttry.config;

import com.example.takeouttry.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        System.out.println("✅ SecurityConfig 构造器注入 JwtAuthenticationFilter 成功");
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        System.out.println("🔧 SecurityFilterChain 开始配置...");

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        // ====================== 完全公开接口 ======================
                        .requestMatchers("/auth/**").permitAll()                    // 登录、注册
                        .requestMatchers("/merchant/register/**").permitAll()
                        .requestMatchers("/dishes/merchant/**").permitAll()         // 用户浏览商家和菜品
                        .requestMatchers("/uploads/**").permitAll()                 // 静态图片资源（Logo、菜品图等）
                        .requestMatchers("/comments/**").permitAll()
                        .requestMatchers("/hello", "/api/test", "/error").permitAll()

                        // ====================== 需要登录的接口 ======================
                        // 用户端
                        .requestMatchers("/cart/**").authenticated()
                        .requestMatchers("/orders/user/**").authenticated()
                        .requestMatchers("/user/**").authenticated()

                        // 商家端
                        .requestMatchers("/merchant/profile").authenticated()
                        .requestMatchers("/merchant/logo").authenticated()
                        .requestMatchers("/orders/merchant/**").authenticated()
                        .requestMatchers("/merchant/dishes/**").authenticated()
                        .requestMatchers("/merchant/categories/**").authenticated()

                        .requestMatchers("/ausers/avatar").authenticated()
                        .requestMatchers("/ausers/update").authenticated()

                        // 管理员端（后续可再细分）
                        .requestMatchers("/ausers/**").hasRole("ADMIN")   // 只有管理员可访问

                        // 其他所有请求都需要登录
                        .anyRequest().authenticated()
                )

                // 添加 JWT 过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        System.out.println(" SecurityFilterChain 配置完成，JWT过滤器已添加");

        return http.build();
    }

    // CORS 配置
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));   // 你的前端地址
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}