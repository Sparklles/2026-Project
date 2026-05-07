package com.example.productmanagement.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.productmanagement.config.PasswordResetProperties;
import com.example.productmanagement.dto.ForgotPasswordAccountDto;
import com.example.productmanagement.dto.ResetPasswordByCodeDto;
import com.example.productmanagement.entity.User;
import com.example.productmanagement.exception.BizIllegalException;
import com.example.productmanagement.result.ResultCodeEnum;
import com.example.productmanagement.service.MailService;
import com.example.productmanagement.service.PasswordResetService;
import com.example.productmanagement.service.UserService;
import com.example.productmanagement.utils.RsaCryptoUtil;
import com.example.productmanagement.vo.MaskedEmailVo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

/**
 * 忘记密码服务实现
 */
@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {

    private static final int PASSWORD_MIN_LEN = 6;

    private final UserService userService;
    private final MailService mailService;
    private final StringRedisTemplate stringRedisTemplate;
    private final PasswordResetProperties passwordResetProperties;

    @Override
    public MaskedEmailVo getMaskedEmail(ForgotPasswordAccountDto dto) {
        User user = getUserByAccount(dto);
        String email = getValidatedEmail(user);
        return new MaskedEmailVo(maskEmail(email));
    }

    @Override
    public MaskedEmailVo sendResetCode(ForgotPasswordAccountDto dto) {
        User user = getUserByAccount(dto);
        String email = getValidatedEmail(user);

        String lockKey = buildSendLockKey(user.getId());
        Boolean success = stringRedisTemplate.opsForValue().setIfAbsent(
                lockKey,
                "1",
                passwordResetProperties.getSendCooldownSeconds(),
                TimeUnit.SECONDS
        );
        if (!Boolean.TRUE.equals(success)) {
            throw new BizIllegalException(ResultCodeEnum.VERIFY_CODE_SEND_TOO_FREQUENT);
        }

        String code = buildVerifyCode();
        String codeKey = buildCodeKey(user.getId());

        try {
            stringRedisTemplate.opsForValue().set(
                    codeKey,
                    code,
                    passwordResetProperties.getCodeExpireMinutes(),
                    TimeUnit.MINUTES
            );

            String content = buildMailContent(code);
            mailService.sendTextMail(email, passwordResetProperties.getMailSubject(), content);
        } catch (Exception e) {
            stringRedisTemplate.delete(codeKey);
            stringRedisTemplate.delete(lockKey);
            throw e;
        }

        return new MaskedEmailVo(maskEmail(email));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(ResetPasswordByCodeDto dto) {
        User user = getUserByAccount(dto.getAccount(), dto.getType());

        if (dto.getCode() == null || dto.getCode().isBlank()) {
            throw new BizIllegalException(ResultCodeEnum.PARAM_ERROR);
        }
        if (dto.getNewPassword() == null || dto.getNewPassword().isBlank()) {
            throw new BizIllegalException(ResultCodeEnum.LOGIN_PASSWORD_EMPTY);
        }

        String codeKey = buildCodeKey(user.getId());
        String cachedCode = stringRedisTemplate.opsForValue().get(codeKey);
        if (cachedCode == null || cachedCode.isBlank()) {
            throw new BizIllegalException(ResultCodeEnum.VERIFY_CODE_EXPIRED);
        }
        if (!cachedCode.equals(dto.getCode().trim())) {
            throw new BizIllegalException(ResultCodeEnum.VERIFY_CODE_ERROR);
        }

        String plainNewPassword;
        try {
            plainNewPassword = RsaCryptoUtil.decrypt(dto.getNewPassword());
        } catch (Exception e) {
            throw new RuntimeException("密码解密失败，请检查加密方式");
        }

        if (plainNewPassword.length() < PASSWORD_MIN_LEN) {
            throw new BizIllegalException(701, "密码长度不能少于 " + PASSWORD_MIN_LEN + " 位");
        }

        String hashedNewPassword = BCrypt.hashpw(plainNewPassword, BCrypt.gensalt());

        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<User>()
                .eq(User::getId, user.getId())
                .set(User::getPassword, hashedNewPassword);

        userService.update(wrapper);
        stringRedisTemplate.delete(codeKey);
    }

    private User getUserByAccount(ForgotPasswordAccountDto dto) {
        if (dto == null) {
            throw new BizIllegalException(ResultCodeEnum.PARAM_ERROR);
        }
        return getUserByAccount(dto.getAccount(), dto.getType());
    }

    private User getUserByAccount(String account, Integer type) {
        if (account == null || account.isBlank()) {
            throw new BizIllegalException(ResultCodeEnum.LOGIN_ACCOUNT_EMPTY);
        }
        if (type == null || (type != 1 && type != 2)) {
            throw new BizIllegalException(ResultCodeEnum.LOGIN_TYPE_ERROR);
        }

        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        if (type == 1) {
            queryWrapper.eq(User::getPhone, account);
        } else {
            queryWrapper.eq(User::getLoginAccount, account);
        }

        User user = userService.getOne(queryWrapper);
        if (user == null) {
            throw new BizIllegalException(ResultCodeEnum.ACCOUNT_NOT_EXIST_ERROR);
        }
        if (user.getRole() == null || user.getRole() != 1) {
            throw new BizIllegalException(ResultCodeEnum.ACCESS_FORBIDDEN);
        }
        return user;
    }

    private String getValidatedEmail(User user) {
        String email = user.getEmail();
        if (email == null || email.isBlank()) {
            throw new BizIllegalException(ResultCodeEnum.EMAIL_NOT_BIND_ERROR);
        }
        return email.trim();
    }

    private String buildCodeKey(Long userId) {
        return "forgot:password:code:" + userId;
    }

    private String buildSendLockKey(Long userId) {
        return "forgot:password:send-lock:" + userId;
    }

    private String buildVerifyCode() {
        int code = (int) ((Math.random() * 9 + 1) * 100000);
        return String.valueOf(code);
    }

    private String buildMailContent(String code) {
        return "您正在找回航海书籍商城账号密码，本次验证码为："
                + code
                + "，"
                + passwordResetProperties.getCodeExpireMinutes()
                + " 分钟内有效。若非本人操作，请忽略此邮件。";
    }

    private String maskEmail(String email) {
        int atIndex = email.indexOf("@");
        if (atIndex <= 1) {
            return email;
        }
        String prefix = email.substring(0, atIndex);
        String domain = email.substring(atIndex);
        if (prefix.length() <= 2) {
            return prefix.charAt(0) + "***" + domain;
        }
        return prefix.substring(0, 2) + "***" + prefix.substring(prefix.length() - 1) + domain;
    }
}
