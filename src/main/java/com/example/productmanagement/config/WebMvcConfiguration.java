package com.example.productmanagement.config;

import com.example.productmanagement.interceptor.AdminAuthorizationInterceptor;
import com.example.productmanagement.interceptor.AuthenticationInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    @Autowired
    private AuthenticationInterceptor authenticationInterceptor;

    @Autowired
    private AdminAuthorizationInterceptor adminAuthorizationInterceptor;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders(
                        AuthenticationInterceptor.ACCESS_TOKEN_HEADER,
                        AuthenticationInterceptor.USER_TOKEN_HEADER,
                        AuthenticationInterceptor.ADMIN_TOKEN_HEADER
                )
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        // ---- 第一道：认证拦截器（解析 token，写入 UserHolder） ----
        // 排除登录和注册接口（无 token）
        registry.addInterceptor(authenticationInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/api/register/**",   // 注册接口
                        "/api/login/**",
                        "/api/auth/public-key",
                        "/api/front/recommend/home",
                        "/api/front/recommend/also-bought",
                        "/images/**",
                        "/favicon.ico",   // 🌟 核心修复：放行浏览器自动请求的图标
                        "/*.html",        // 放行可能存在的静态网页
                        "/error"
                )
                .order(1);

        // ---- 第二道：管理员权限拦截器（校验 role == 2） ----
        // 仅对 /admin/** 路径生效，在认证拦截器之后执行
        registry.addInterceptor(adminAuthorizationInterceptor)
                .addPathPatterns(
                        "/api/admin/**",
                        "/api/order/admin/**",
                        "/api/refund/admin/**"
                )
                .order(2);
    }
}

