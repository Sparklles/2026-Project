package com.example.productmanagement.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 忘记密码验证码配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.password-reset")
public class PasswordResetProperties {

    /**
     * 验证码有效期（分钟）
     */
    private Integer codeExpireMinutes = 5;

    /**
     * 发送冷却时间（秒）
     */
    private Integer sendCooldownSeconds = 60;

    /**
     * 邮件主题
     */
    private String mailSubject = "航海书籍商城验证码";
}
