package com.example.productmanagement.interceptor;

import com.example.productmanagement.utils.UserHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 管理员权限拦截器
 *
 * <p>仅拦截 {@code /admin/**} 路径。
 * 前置条件：{@link AuthenticationInterceptor} 已执行，
 * {@link UserHolder} 中已存入当前用户信息。
 *
 * <p>校验规则：token 中的 role 必须为 2（管理员），否则返回 403。
 */
@Component
public class AdminAuthorizationInterceptor implements HandlerInterceptor {

    /** 管理员角色值 */
    private static final int ADMIN_ROLE = 2;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        Integer role = UserHolder.getRole();

        if (role == null || role != ADMIN_ROLE) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);   // 403
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":403,\"message\":\"无权限，仅管理员可访问\"}");
            return false;
        }
        return true;
    }
}
