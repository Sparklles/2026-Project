package com.example.productmanagement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.example.productmanagement.interceptor.AuthenticationInterceptor;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class GlobalCorsConfiguration implements WebMvcConfigurer {

    @Bean
    public CorsFilter corsFilter() {
        // 1. 构件跨域配置对象
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        // 允许的源（允许访问的客户端具体域名，如“https://www.mobanche.fun”，*表示所有域名）
        corsConfiguration.addAllowedOriginPattern("*");
        // 允许携带cookie，前端需配置withCredential: true
        corsConfiguration.setAllowCredentials(true);
        // 设置允许的请求头(*表示允许所有)
        corsConfiguration.addAllowedHeader("*");
        // 设置允许的请求方法(GET/DELETE/POST/UPDATE，*表示允许所有)
        corsConfiguration.addAllowedMethod("*");
        // 允许前端读取自动续签后的 token 响应头
        corsConfiguration.addExposedHeader(AuthenticationInterceptor.ACCESS_TOKEN_HEADER);
        corsConfiguration.addExposedHeader(AuthenticationInterceptor.USER_TOKEN_HEADER);
        corsConfiguration.addExposedHeader(AuthenticationInterceptor.ADMIN_TOKEN_HEADER);
        // 预检请求（OPTIONS）的缓存时间(秒)，避免平凡发送预检请求
        corsConfiguration.setMaxAge(3600L);

        // 2。 配置跨域规则生效的接口路径，/**表示所有接口
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);

        return new CorsFilter(source);
    }
}

