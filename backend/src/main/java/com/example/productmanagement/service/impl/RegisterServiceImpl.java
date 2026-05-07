package com.example.productmanagement.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.productmanagement.exception.BizIllegalException;
import com.example.productmanagement.result.ResultCodeEnum;
import com.example.productmanagement.service.RegisterService;
import com.example.productmanagement.dto.RegisterDto;
import com.example.productmanagement.entity.User;
import com.example.productmanagement.entity.UserDetail;
import com.example.productmanagement.service.UserDetailService;
import com.example.productmanagement.service.UserService;
import com.example.productmanagement.utils.RsaCryptoUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 认证服务实现类
 */
@Service
@RequiredArgsConstructor
public class RegisterServiceImpl implements RegisterService {

    /** 手机号正则：大陆11位手机号 */
    private static final String PHONE_REGEX = "^1[3-9]\\d{9}$";

    /** 账号正则：4-20位字母或数字（至少含一个字母） */
    private static final String ACCOUNT_REGEX = "^(?=.*[a-zA-Z])[a-zA-Z0-9]{4,20}$";

    /** 密码最小长度 */
    private static final int PASSWORD_MIN_LEN = 6;

    private final UserService userService;
    private final UserDetailService userDetailService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterDto registerDto) {

        // ---- 1. 基础参数校验 ----
        if (registerDto.getAccount() == null || registerDto.getAccount().isBlank()) {
            throw new BizIllegalException(ResultCodeEnum.LOGIN_ACCOUNT_EMPTY);
        }
        if (registerDto.getPassword() == null || registerDto.getPassword().isBlank()) {
            throw new BizIllegalException(ResultCodeEnum.LOGIN_PASSWORD_EMPTY);
        }
        if (registerDto.getType() == null||(registerDto.getType() != 1 && registerDto.getType() != 2)) {
            throw new BizIllegalException(ResultCodeEnum.REGISTER_TYPE_ERROR);
        }


        // ---- 2. RSA 解密密码明文 ----
//        String plainPassword=registerDto.getPassword();
        String plainPassword;
        //测试阶段先直接存储明文
        try {
            plainPassword = RsaCryptoUtil.decrypt(registerDto.getPassword());
        } catch (Exception e) {
            throw new RuntimeException("密码解密失败，请检查加密方式");
        }

        // ---- 3. 密码强度校验 ----
        if (plainPassword.length() < PASSWORD_MIN_LEN) {
            throw new BizIllegalException(701,"密码长度不能少于 " + PASSWORD_MIN_LEN + " 位");
        }

        // ---- 4. 账号格式校验 & 唯一性校验 ----
        User user = new User();

        if (registerDto.getType() == 1) {
            // 手机号注册
            String phone = registerDto.getAccount();
            if (!phone.matches(PHONE_REGEX)) {
                throw new BizIllegalException(ResultCodeEnum.PHONE_ERROR);
            }
            // 手机号唯一性
            boolean phoneExists = userService.count(
                    new LambdaQueryWrapper<User>().eq(User::getPhone, phone)) > 0;
            if (phoneExists) {
                throw new BizIllegalException(ResultCodeEnum.ACCOUNT_EXIST_ERROR);
            }
            // 手机号注册时自动以手机号为 login_account（可后续由用户修改）
            user.setPhone(phone);
            user.setLoginAccount(phone);

        } else {
            // 账号注册（字母+数字，至少含一个字母，4-20位）
            String account = registerDto.getAccount();
            if (!account.matches(ACCOUNT_REGEX)) {
                throw new BizIllegalException(702,"账号需为 4-20 位字母或数字，且至少包含一个字母");
            }
            // 账号唯一性
            boolean accountExists = userService.count(
                    new LambdaQueryWrapper<User>().eq(User::getLoginAccount, account)) > 0;
            if (accountExists) {
                throw new BizIllegalException(ResultCodeEnum.ACCOUNT_EXIST_ERROR);
            }
            user.setLoginAccount(account);
        }

        // ---- 5. BCrypt 哈希加密密码后存库 ----
        String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
        user.setPassword(hashedPassword);

        // 设置默认字段
        user.setRole(1);    // 普通用户
        user.setStatus(1);  // 正常状态
        user.setIsDeleted(0);

        // ---- 6. 保存 user 记录（雪花 ID 由 MyBatis-Plus 自动生成） ----
        userService.save(user);

        // ---- 7. 同步创建 user_detail 记录（初始化为空详情） ----
        UserDetail userDetail = new UserDetail();
        userDetail.setUserId(user.getId());
        userDetail.setGender(0); // 默认性别保密
        String account = registerDto.getAccount();
        String suffix = account.length() >= 4 ? account.substring(account.length() - 4) : account;
        userDetail.setNickname("用户" + suffix);
        userDetailService.save(userDetail);
    }
}
