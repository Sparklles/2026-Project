package com.example.productmanagement.service;

import com.example.productmanagement.dto.RegisterDto;

/**
 * 认证服务接口（注册 / 注销等鉴权相关操作）
 */
public interface RegisterService {

    /**
     * 用户注册
     *
     * <p>支持两种注册方式：
     * <ul>
     *   <li>type=1：手机号 + 密码注册</li>
     *   <li>type=2：自定义账号（字母+数字）+ 密码注册</li>
     * </ul>
     * 注册成功后同步创建 user_detail 记录。
     *
     * @param registerDto 注册请求参数
     */
    void register(RegisterDto registerDto);
}
