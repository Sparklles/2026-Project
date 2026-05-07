package com.example.productmanagement.utils;

public class UserHolder {

    private static final ThreadLocal<LoginUserInfo> threadLocal = new ThreadLocal<>();

    /**
     * 将当前登录用户信息存入 ThreadLocal。
     *
     * @param loginUserInfo 包含 userId 和 role 的用户上下文对象
     */
    public static void setLoginUser(LoginUserInfo loginUserInfo) {
        threadLocal.set(loginUserInfo);
    }

    /**
     * 获取当前登录用户上下文信息。
     *
     * @return {@link LoginUserInfo}，包含 userId 和 role
     */
    public static LoginUserInfo getLoginUser() {
        return threadLocal.get();
    }

    /**
     * 获取当前登录用户 ID（快捷方法）。
     *
     * @return 用户主键 ID
     */
    public static Long getUserId() {
        LoginUserInfo info = threadLocal.get();
        return info == null ? null : info.getUserId();
    }

    /**
     * 获取当前登录用户角色（快捷方法）。
     *
     * @return 角色值：1-普通用户，2-管理员
     */
    public static Integer getRole() {
        LoginUserInfo info = threadLocal.get();
        return info == null ? null : info.getRole();
    }

    /**
     * 请求结束后清理 ThreadLocal，防止内存泄漏。
     */
    public static void clear() {
        threadLocal.remove();
    }
}
