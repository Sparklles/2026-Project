package com.example.productmanagement.controller.profile;

import com.example.productmanagement.dto.UpdateProfileDto;
import com.example.productmanagement.dto.UpdateUserAccountDto;
import com.example.productmanagement.service.ProfileService;
import com.example.productmanagement.result.Result;
import com.example.productmanagement.vo.UserProfileVo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端 - 用户信息控制器
 *
 * <p>所有接口需携带有效 Token（role=2 管理员），
 * 路由层以 {@code /admin} 前缀与用户端区分。
 *
 * <pre>
 * GET  /admin/profile/me                查询管理员自己的个人信息（复用共享逻辑）
 * PUT  /admin/profile/me                修改管理员自己的详情信息（复用共享逻辑）
 * GET  /admin/profile/users/{userId}    查询任意用户的完整信息
 * PUT  /admin/profile/users/{userId}    修改任意用户的账号核心字段
 * </pre>
 */
@RestController
@RequestMapping("/api/admin/profile")
@RequiredArgsConstructor
public class AdminProfileController {

    private final ProfileService profileService;

    /**
     * 管理员查询自己的个人信息（与用户端共用 Service 方法）。
     */
    @GetMapping("/me")
    public Result<UserProfileVo> getMyProfile() {
        return Result.ok(profileService.getMyProfile());
    }

    /**
     * 管理员修改自己的 user_detail 信息（与用户端共用 Service 方法）。
     */
    @PutMapping("/me")
    public Result<Void> updateMyProfile(@RequestBody UpdateProfileDto dto) {
        profileService.updateMyProfile(dto);
        return Result.ok();
    }

    /**
     * 管理端：查询任意用户的完整信息。
     *
     * @param userId 目标用户 ID
     */
    @GetMapping("/users/{userId}")
    public Result<UserProfileVo> getUserProfile(@PathVariable Long userId) {
        // 不允许通过此接口查询其他管理员（可视业务需求放开）
        return Result.ok(profileService.getUserProfileById(userId));
    }

    /**
     * 管理端：修改任意用户的账号核心字段（loginAccount/phone/email/status）。
     * 不允许修改自身以外的管理员账号。
     *
     * @param userId 目标用户 ID
     * @param dto    待更新字段
     */
    @PutMapping("/users/{userId}")
    public Result<Void> updateUserAccount(@PathVariable Long userId,
                                          @RequestBody UpdateUserAccountDto dto) {
        profileService.updateUserAccount(userId, dto);
        return Result.ok();
    }
}
