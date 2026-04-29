package com.example.productmanagement.controller.profile;

import com.example.productmanagement.dto.UpdateAccountDto;
import com.example.productmanagement.dto.UpdatePasswordDto;
import com.example.productmanagement.dto.UpdateProfileDto;
import com.example.productmanagement.service.ProfileService;
import com.example.productmanagement.result.Result;
import com.example.productmanagement.vo.UserProfileVo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户端 - 个人信息控制器
 *
 * <p>所有接口需携带有效 Token（role=1 普通用户），由拦截器统一鉴权。
 *
 * <pre>
 * GET  /profile/me          查询自己的个人信息
 * PUT  /profile/me          修改自己的详情信息（昵称/头像/性别/生日/签名）
 * </pre>
 */
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final ProfileService profileService;

    /**
     * 查询当前登录用户的个人信息。
     */
    @GetMapping("/me")
    public Result<UserProfileVo> getMyProfile() {
        return Result.ok(profileService.getMyProfile());
    }

    /**
     * 修改当前登录用户的个人详情（user_detail 表字段）。
     * 仅传入需要修改的字段，未传入的字段保持不变。
     */
    @PutMapping("/me")
    public Result<Void> updateMyProfile(@RequestBody UpdateProfileDto dto) {
        profileService.updateMyProfile(dto);
        return Result.ok();
    }

    @PutMapping("/password")
    public Result<Void> updateMyPassword(@RequestBody UpdatePasswordDto dto) {
        profileService.updateMyPassword(dto);
        return Result.ok();
    }
    @PutMapping("/email")
    public Result<Void> updateMyEmail(@RequestBody UpdateAccountDto dto) {
        String email = dto.getEmail();
        profileService.updateMyEmail(email);
        return Result.ok();
    }
}
