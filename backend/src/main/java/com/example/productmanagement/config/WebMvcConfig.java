package com.example.productmanagement.config;

import com.example.productmanagement.interceptor.AuthenticationInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 允许所有接口
                .allowedOriginPatterns("*") // 允许所有前端地址跨域访问
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders(
                        AuthenticationInterceptor.ACCESS_TOKEN_HEADER,
                        AuthenticationInterceptor.USER_TOKEN_HEADER,
                        AuthenticationInterceptor.ADMIN_TOKEN_HEADER
                )
                .allowCredentials(true)
                .maxAge(3600); // 跨域允许时间
    }
}
