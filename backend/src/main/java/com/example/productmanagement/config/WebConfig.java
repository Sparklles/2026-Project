package com.example.productmanagement.config;

import com.example.productmanagement.interceptor.AuthenticationInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 获取项目运行根目录
        String projectPath = System.getProperty("user.dir");

        // 映射规则：当浏览器请求 http://localhost:8080/images/xxx.png 时
        // 去找项目根目录下的 uploads/xxx.png 文件
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + projectPath + File.separator + "uploads" + File.separator);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 🌟 必须包含 DELETE
                .exposedHeaders(
                        AuthenticationInterceptor.ACCESS_TOKEN_HEADER,
                        AuthenticationInterceptor.USER_TOKEN_HEADER,
                        AuthenticationInterceptor.ADMIN_TOKEN_HEADER
                )
                .maxAge(3600);
    }
}
