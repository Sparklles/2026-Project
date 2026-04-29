package com.example.productmanagement.interceptor;


import com.example.productmanagement.utils.JwtUtil;
import com.example.productmanagement.utils.LoginUserInfo;
import com.example.productmanagement.utils.UserHolder;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthenticationInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = request.getHeader("access-token");
        Claims claims = JwtUtil.parseToken(token);

        Long userId = claims.get("userId", Long.class);
        Integer role = claims.get("role", Integer.class);

        UserHolder.setLoginUser(new LoginUserInfo(userId, role));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserHolder.clear();
    }
}