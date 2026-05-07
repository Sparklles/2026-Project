package com.example.productmanagement.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 脱敏邮箱展示 VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaskedEmailVo {

    /**
     * 脱敏后的邮箱
     */
    private String maskedEmail;
}
