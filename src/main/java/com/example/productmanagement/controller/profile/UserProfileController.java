package com.example.productmanagement.controller.profile;

import com.example.productmanagement.dto.UpdateAccountDto;
import com.example.productmanagement.dto.UpdatePasswordDto;
import com.example.productmanagement.dto.UpdateProfileDto;
import com.example.productmanagement.service.ProfileService;
import com.example.productmanagement.result.Result;
import com.example.productmanagement.vo.UserProfileVo;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * 用户端 - 个人信息控制器
 *
 * <p>所有接口需携带有效 Token（role=1 普通用户），由拦截器统一鉴权。
 *
 * <pre>
 * GET  /profile/me                  查询自己的个人信息
 * PUT  /profile/me                  修改自己的详情信息（昵称/头像/性别/生日/签名）
 * POST /profile/avatar/upload       上传头像图片并返回可访问 URL
 * </pre>
 */
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private static final long MAX_AVATAR_SIZE = 5 * 1024 * 1024;
    private static final Set<String> ALLOWED_AVATAR_SUFFIXES = Set.of(".jpg", ".jpeg", ".png", ".webp", ".gif");

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

    /**
     * 上传当前用户头像图片。
     *
     * <p>该接口只负责把图片保存到本地 uploads 目录，并返回 /images/** 可访问 URL；
     * 不负责写入 user_detail.avatar_url。前端拿到 URL 后，再调用 PUT /api/profile/me 保存头像。</p>
     */
    @PostMapping("/avatar/upload")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        if (file == null || file.isEmpty()) {
            return Result.error(400, "上传文件不能为空");
        }
        if (file.getSize() > MAX_AVATAR_SIZE) {
            return Result.error(400, "头像图片不能超过5MB");
        }

        String originalFilename = file.getOriginalFilename();
        String suffix = getSafeSuffix(originalFilename);
        if (suffix == null || !ALLOWED_AVATAR_SUFFIXES.contains(suffix)) {
            return Result.error(400, "仅支持 jpg、jpeg、png、webp、gif 格式头像");
        }

        try {
            String projectPath = System.getProperty("user.dir");
            String uploadDir = projectPath + File.separator + "uploads";

            File dir = new File(uploadDir);
            if (!dir.exists() && !dir.mkdirs()) {
                return Result.error(500, "上传目录创建失败");
            }

            String newFileName = "avatar_" + UUID.randomUUID().toString().replace("-", "") + suffix;
            File targetFile = new File(uploadDir, newFileName);
            file.transferTo(targetFile);
            String serverUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
            return Result.ok(serverUrl + "/images/" + newFileName);
        } catch (IOException e) {
            return Result.error(500, "头像上传失败：" + e.getMessage());
        }
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

    private String getSafeSuffix(String filename) {
        if (filename == null) {
            return null;
        }
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filename.length() - 1) {
            return null;
        }
        return filename.substring(dotIndex).toLowerCase(Locale.ROOT);
    }
}

