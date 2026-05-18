package com.example.productmanagement.service;

import com.example.productmanagement.dto.UpdatePasswordDto;
import com.example.productmanagement.dto.UpdateProfileDto;
import com.example.productmanagement.dto.UpdateUserAccountDto;
import com.example.productmanagement.dto.UpdateUserStatusDto;
import com.example.productmanagement.vo.UserProfileVo;

/**
 * 个人信息服务接口
 */
public interface ProfileService {

    /**
     * 查询当前登录用户的个人信息（用户端 & 管理端共用）。
     *
     * @return 聚合的用户信息 VO
     */
    UserProfileVo getMyProfile();

    /**
     * 修改当前登录用户的 user_detail 信息（用户端 & 管理端共用）。
     * 可修改字段：昵称、头像、性别、生日、签名。
     *
     * @param dto 待更新的详情字段
     */
    void updateMyProfile(UpdateProfileDto dto);

    // -------- 以下为管理端独占接口 --------

    /**
     * 管理端：根据 userId 查询任意用户的完整信息。
     *
     * @param userId 目标用户 ID
     * @return 聚合的用户信息 VO
     */
    UserProfileVo getUserProfileById(Long userId);

    /**
     * 管理端：修改任意用户的账号核心字段（loginAccount、phone、email、status）。
     *
     * @param userId 目标用户 ID
     * @param dto    待更新的账号字段
     */
    void updateUserAccount(Long userId, UpdateUserAccountDto dto);

    /**
     * 管理端：冻结/解冻指定用户账号。
     *
     * @param userId 目标用户 ID
     * @param dto    账号状态参数
     */
    void updateUserStatus(Long userId, UpdateUserStatusDto dto);

    void updateMyPassword(UpdatePasswordDto dto);

    void updateMyEmail(String email);
}
