package com.example.productmanagement.controller.auth;

import com.example.productmanagement.dto.ForgotPasswordAccountDto;
import com.example.productmanagement.dto.ResetPasswordByCodeDto;
import com.example.productmanagement.result.Result;
import com.example.productmanagement.service.PasswordResetService;
import com.example.productmanagement.vo.MaskedEmailVo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 忘记密码控制器
 */
@RestController
@RequestMapping("/api/password/forgot")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    /**
     * 根据账号查询绑定邮箱的脱敏展示值。
     */
    @PostMapping("/email")
    public Result<MaskedEmailVo> getMaskedEmail(@RequestBody ForgotPasswordAccountDto dto) {
        return Result.ok(passwordResetService.getMaskedEmail(dto));
    }

    /**
     * 向账号绑定邮箱发送验证码。
     */
    @PostMapping("/code")
    public Result<MaskedEmailVo> sendResetCode(@RequestBody ForgotPasswordAccountDto dto) {
        return Result.ok(passwordResetService.sendResetCode(dto));
    }

    /**
     * 校验验证码并重置密码。
     */
    @PostMapping("/reset")
    public Result<Void> resetPassword(@RequestBody ResetPasswordByCodeDto dto) {
        passwordResetService.resetPassword(dto);
        return Result.ok();
    }
}
