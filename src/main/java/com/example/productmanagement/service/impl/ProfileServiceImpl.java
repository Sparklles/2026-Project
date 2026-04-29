package com.example.productmanagement.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;

import com.example.productmanagement.dto.UpdatePasswordDto;
import com.example.productmanagement.dto.UpdateProfileDto;
import com.example.productmanagement.dto.UpdateUserAccountDto;
import com.example.productmanagement.entity.User;
import com.example.productmanagement.entity.UserDetail;
import com.example.productmanagement.exception.BizIllegalException;
import com.example.productmanagement.result.ResultCodeEnum;
import com.example.productmanagement.service.ProfileService;
import com.example.productmanagement.service.UserDetailService;
import com.example.productmanagement.service.UserService;
import com.example.productmanagement.utils.RsaCryptoUtil;
import com.example.productmanagement.utils.UserHolder;
import com.example.productmanagement.vo.UserProfileVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 个人信息服务实现类
 */
@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final UserService userService;
    private final UserDetailService userDetailService;

    // ----------------------------------------------------------------
    //  共用接口实现
    // ----------------------------------------------------------------

    @Override
    public UserProfileVo getMyProfile() {
        Long userId = UserHolder.getUserId();
        return buildProfileVo(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMyProfile(UpdateProfileDto dto) {
        Long userId = UserHolder.getUserId();
        updateDetailByUserId(userId, dto);
    }

    // ----------------------------------------------------------------
    //  管理端独占接口实现
    // ----------------------------------------------------------------

    @Override
    public UserProfileVo getUserProfileById(Long userId) {
        return buildProfileVo(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserAccount(Long userId, UpdateUserAccountDto dto) {
        User user = userService.getById(userId);
        if (user == null) {
            throw new BizIllegalException(ResultCodeEnum.ACCOUNT_NOT_EXIST_ERROR);
        }

        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<User>()
                .eq(User::getId, userId);

        boolean hasUpdate = false;

        if (dto.getLoginAccount() != null && !dto.getLoginAccount().isBlank()) {
            // 账号唯一性校验
            boolean exists = userService.lambdaQuery()
                    .eq(User::getLoginAccount, dto.getLoginAccount())
                    .ne(User::getId, userId)
                    .count() > 0;
            if (exists) {
                throw new BizIllegalException(ResultCodeEnum.ACCOUNT_EXIST_ERROR);
            }
            wrapper.set(User::getLoginAccount, dto.getLoginAccount());
            hasUpdate = true;
        }

        if (dto.getPhone() != null && !dto.getPhone().isBlank()) {
            boolean exists = userService.lambdaQuery()
                    .eq(User::getPhone, dto.getPhone())
                    .ne(User::getId, userId)
                    .count() > 0;
            if (exists) {
                throw new BizIllegalException(ResultCodeEnum.ACCOUNT_EXIST_ERROR);
            }
            wrapper.set(User::getPhone, dto.getPhone());
            hasUpdate = true;
        }

        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            wrapper.set(User::getEmail, dto.getEmail());
            hasUpdate = true;
        }

        if (dto.getStatus() != null) {
            if (dto.getStatus() != 0 && dto.getStatus() != 1) {
                throw new BizIllegalException(ResultCodeEnum.PARAM_ERROR);
            }
            wrapper.set(User::getStatus, dto.getStatus());
            hasUpdate = true;
        }

        if (hasUpdate) {
            userService.update(wrapper);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMyPassword(UpdatePasswordDto dto) {
        Long userId = UserHolder.getUserId();

        if (dto.getOldPassword() == null || dto.getOldPassword().isBlank()) {
            throw new BizIllegalException(ResultCodeEnum.LOGIN_PASSWORD_EMPTY);
        }
        if (dto.getNewPassword() == null || dto.getNewPassword().isBlank()) {
            throw new BizIllegalException(ResultCodeEnum.LOGIN_PASSWORD_EMPTY);
        }

        User user = userService.getById(userId);
        if (user == null) {
            throw new BizIllegalException(ResultCodeEnum.ACCOUNT_NOT_EXIST_ERROR);
        }

        String plainOldPassword;
        String plainNewPassword;

        try {
            plainOldPassword = RsaCryptoUtil.decrypt(dto.getOldPassword());
            plainNewPassword = RsaCryptoUtil.decrypt(dto.getNewPassword());
        } catch (Exception e) {
            throw new RuntimeException("系统异常，稍后再试");
        }

        if (plainNewPassword.length() < 6) {
            throw new BizIllegalException(701,"密码长度不能小于6位");
        }

        if (!BCrypt.checkpw(plainOldPassword, user.getPassword())) {
            throw new BizIllegalException(ResultCodeEnum.ACCOUNT_PASSWORD_ERROR);
        }

        String hashedNewPassword = BCrypt.hashpw(plainNewPassword, BCrypt.gensalt());

        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<User>()
                .eq(User::getId, userId)
                .set(User::getPassword, hashedNewPassword);

        userService.update(wrapper);
    }

    @Override
    public void updateMyEmail(String email) {
        Long userId = UserHolder.getUserId();

        if (email == null || email.isBlank()) {
            throw new BizIllegalException(ResultCodeEnum.EMAIL_ERROR);
        }

        String trimmedEmail = email.trim();

        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        if (!trimmedEmail.matches(emailRegex)) {
            throw new BizIllegalException(ResultCodeEnum.EMAIL_ERROR);
        }

        if (trimmedEmail.length() > 100) {
            throw new BizIllegalException(ResultCodeEnum.EMAIL_LENGTH_ERROR);
        }

        boolean emailExists = userService.lambdaQuery()
                .eq(User::getEmail, trimmedEmail)
                .ne(User::getId, userId)
                .count() > 0;

        if (emailExists) {
            throw new BizIllegalException(ResultCodeEnum.EMAIL_EXIST_ERROR);
        }

        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<User>()
                .eq(User::getId, userId)
                .set(User::getEmail, trimmedEmail);

        userService.update(wrapper);
    }


    // ----------------------------------------------------------------
    //  私有工具方法
    // ----------------------------------------------------------------

    /**
     * 聚合 user + user_detail 组装 UserProfileVo。
     */
    private UserProfileVo buildProfileVo(Long userId) {
        User user = userService.getById(userId);
        if (user == null) {
            throw new BizIllegalException(ResultCodeEnum.ACCOUNT_NOT_EXIST_ERROR);
        }

        UserDetail detail = userDetailService.getById(userId);

        UserProfileVo vo = new UserProfileVo();

        // 来自 user 表
        vo.setUserId(user.getId());
        vo.setLoginAccount(user.getLoginAccount());
        vo.setPhone(maskPhone(user.getPhone()));
        vo.setEmail(user.getEmail());
        vo.setRole(user.getRole());
        vo.setStatus(user.getStatus());
        vo.setLastLoginTime(user.getLastLoginTime());
        vo.setCreateTime(user.getCreateTime());

        // 来自 user_detail 表（detail 可能为 null）
        if (detail != null) {
            vo.setNickname(detail.getNickname());
            vo.setAvatarUrl(detail.getAvatarUrl());
            vo.setGender(detail.getGender());
            vo.setBirthday(detail.getBirthday());
            vo.setSignature(detail.getSignature());
        }

        return vo;
    }

    /**
     * 根据 userId 更新 user_detail 可编辑字段（null 字段跳过）。
     */
    private void updateDetailByUserId(Long userId, UpdateProfileDto dto) {
        LambdaUpdateWrapper<UserDetail> wrapper = new LambdaUpdateWrapper<UserDetail>()
                .eq(UserDetail::getUserId, userId);

        boolean hasUpdate = false;

        if (dto.getNickname() != null) {
            wrapper.set(UserDetail::getNickname, dto.getNickname());
            hasUpdate = true;
        }
        if (dto.getAvatarUrl() != null) {
            wrapper.set(UserDetail::getAvatarUrl, dto.getAvatarUrl());
            hasUpdate = true;
        }
        if (dto.getGender() != null) {
            if (dto.getGender() < 0 || dto.getGender() > 2) {
                throw new BizIllegalException(ResultCodeEnum.PARAM_ERROR);
            }
            wrapper.set(UserDetail::getGender, dto.getGender());
            hasUpdate = true;
        }
        if (dto.getBirthday() != null) {
            wrapper.set(UserDetail::getBirthday, dto.getBirthday());
            hasUpdate = true;
        }
        if (dto.getSignature() != null) {
            wrapper.set(UserDetail::getSignature, dto.getSignature());
            hasUpdate = true;
        }

        if (hasUpdate) {
            userDetailService.update(wrapper);
        }
    }

    /**
     * 手机号脱敏：保留前3位和后4位，中间用 **** 替代。
     * 例：138****5678
     */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}
