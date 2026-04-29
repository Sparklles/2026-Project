package com.example.productmanagement.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.productmanagement.dto.LoginDto;
import com.example.productmanagement.entity.User;
import com.example.productmanagement.exception.BizIllegalException;
import com.example.productmanagement.result.ResultCodeEnum;
import com.example.productmanagement.service.UserService;
import com.example.productmanagement.mapper.UserMapper;
import com.example.productmanagement.utils.JwtUtil;
import com.example.productmanagement.utils.RsaCryptoUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
* @author freedom
* @description 针对表【user(系统用户核心表(鉴权与风控))】的数据库操作Service实现
* @createDate 2026-04-21 15:14:41
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    @Override
    public String login(LoginDto loginDto) {
        // ---- 1. 基础参数校验 ----
        if (loginDto.getAccount() == null) {
            throw new BizIllegalException(ResultCodeEnum.LOGIN_ACCOUNT_EMPTY);
        }
        if (loginDto.getPassword() == null) {
            throw new BizIllegalException(ResultCodeEnum.LOGIN_PASSWORD_EMPTY);
        }
        if (loginDto.getType() == null||(loginDto.getType() != 1 && loginDto.getType() != 2)) {
            throw new BizIllegalException(ResultCodeEnum.LOGIN_TYPE_ERROR);
        }

        if(loginDto.getExpectedRole()==null){
            throw new BizIllegalException(ResultCodeEnum.ILLEGAL_REQUEST);
        }


        // ---- 2. RSA 解密前端传来的密文密码 ----
        //测试阶段先直接发送明文
//        String plainPassword=loginDto.getPassword();
        String plainPassword;
        try {
            plainPassword = RsaCryptoUtil.decrypt(loginDto.getPassword());
        } catch (Exception e) {
            throw new RuntimeException("密码解密失败，请检查加密方式");
        }

        // ---- 3. 根据登录类型查询用户 ----
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        if (loginDto.getType() == 1) {
            // 手机号 + 密码登录
            queryWrapper.eq(User::getPhone, loginDto.getAccount());
        } else {
            // 账号 + 密码登录
            queryWrapper.eq(User::getLoginAccount, loginDto.getAccount());
        }

        User user = this.getOne(queryWrapper);
        if (user == null) {
            throw new BizIllegalException(ResultCodeEnum.ACCOUNT_NOT_EXIST_ERROR);
        }

        // ---- 4. 校验账号状态 ----
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BizIllegalException(ResultCodeEnum.ACCOUNT_DISABLED_ERROR);
        }

        if (!user.getRole().equals(loginDto.getExpectedRole())) {
            throw new BizIllegalException(ResultCodeEnum.ACCESS_FORBIDDEN);
        }

        // ---- 5. 验证密码（BCrypt 哈希比对） ----
        if (!BCrypt.checkpw(plainPassword, user.getPassword())) {
            throw new BizIllegalException(ResultCodeEnum.ACCOUNT_PASSWORD_ERROR);
        }
        // ---- 6. 更新登录时间 ----
        user.setLastLoginTime(LocalDateTime.now());
        this.updateById(user);

        // ---- 7. 生成 JWT Token 并返回（携带 userId 和 role） ----
        return JwtUtil.createJwt(user.getId(), user.getRole());
    }
}




