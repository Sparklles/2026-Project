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

        // 🌟 核心修复：把提取类型改为通用的 Object，然后用 .toString() 转为字符串，再转为 Long
        // 这样无论以前的 Token 存的是数字，还是现在的 Token 存的是字符串，都不会再报错了！
        Object userIdObj = claims.get("userId");
        Long userId = Long.valueOf(userIdObj.toString());
        Integer role = claims.get("role", Integer.class);

        UserHolder.setLoginUser(new LoginUserInfo(userId, role));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserHolder.clear();
    }
}