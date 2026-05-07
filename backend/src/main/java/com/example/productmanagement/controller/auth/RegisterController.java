package com.example.productmanagement.controller.auth;


import com.example.productmanagement.dto.RegisterDto;
import com.example.productmanagement.result.Result;
import com.example.productmanagement.service.RegisterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证模块控制器（注册 / 登录入口）
 *
 * <p>
 * 路由统一挂载在 /auth 前缀下，与业务接口区分。
 */
@RestController
@RequestMapping("/api/register")
@RequiredArgsConstructor
public class RegisterController {

    private final RegisterService registerService;

    /**
     * 用户注册
     *
     * <p>
     * 请求体示例（手机号注册）：
     * 
     * <pre>
     * {
     *   "account": "13800138000",
     *   "password": "&lt;RSA公钥加密后的Base64密文&gt;",
     *   "type": 1
     * }
     * </pre>
     *
     * <p>
     * 请求体示例（账号注册）：
     * 
     * <pre>
     * {
     *   "account": "freedom01",
     *   "password": "&lt;RSA公钥加密后的Base64密文&gt;",
     *   "type": 2
     * }
     * </pre>
     */
    @PostMapping("/user")
    public Result<Void> register(@RequestBody RegisterDto registerDto) {
        registerService.register(registerDto);
        return Result.ok();
    }
}
