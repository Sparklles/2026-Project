package com.example.productmanagement.service;

import com.example.productmanagement.dto.ForgotPasswordAccountDto;
import com.example.productmanagement.dto.ResetPasswordByCodeDto;
import com.example.productmanagement.vo.MaskedEmailVo;

/**
 * 忘记密码服务
 */
public interface PasswordResetService {

    /**
     * 根据账号查询脱敏邮箱。
     *
     * @param dto 账号识别参数
     * @return 脱敏邮箱
     */
    MaskedEmailVo getMaskedEmail(ForgotPasswordAccountDto dto);

    /**
     * 向账号绑定邮箱发送验证码。
     *
     * @param dto 账号识别参数
     * @return 脱敏邮箱
     */
    MaskedEmailVo sendResetCode(ForgotPasswordAccountDto dto);

    /**
     * 校验验证码并重置密码。
     *
     * @param dto 重置密码参数
     */
    void resetPassword(ResetPasswordByCodeDto dto);
}
