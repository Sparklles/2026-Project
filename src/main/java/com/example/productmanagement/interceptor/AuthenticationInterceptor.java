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

    public static final String ACCESS_TOKEN_HEADER = "access-token";
    public static final String USER_TOKEN_HEADER = "user-token";
    public static final String ADMIN_TOKEN_HEADER = "admin-token";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = request.getHeader(ACCESS_TOKEN_HEADER);
        boolean optionalAuth = "/api/front/recommend/personalized".equals(request.getRequestURI());
        if ((token == null || token.isBlank()) && optionalAuth) {
            return true;
        }

        Claims claims;
        try {
            claims = JwtUtil.parseToken(token);
        } catch (Exception e) {
            if (optionalAuth) {
                return true;
            }
            throw e;
        }

        // 🌟 核心修复：把提取类型改为通用的 Object，然后用 .toString() 转为字符串，再转为 Long
        // 这样无论以前的 Token 存的是数字，还是现在的 Token 存的是字符串，都不会再报错了！
        Object userIdObj = claims.get("userId");
        Long userId = Long.valueOf(userIdObj.toString());
        Integer role = claims.get("role", Integer.class);

        UserHolder.setLoginUser(new LoginUserInfo(userId, role));
        if (JwtUtil.shouldRenew(claims)) {
            response.setHeader(getRenewTokenHeader(role), JwtUtil.renewToken(claims));
        }
        return true;
    }

    private String getRenewTokenHeader(Integer role) {
        return role != null && role == 2 ? ADMIN_TOKEN_HEADER : USER_TOKEN_HEADER;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserHolder.clear();
    }
}
