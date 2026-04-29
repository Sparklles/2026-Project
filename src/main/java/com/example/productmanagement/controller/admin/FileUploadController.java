package com.example.productmanagement.controller.admin;


import com.example.productmanagement.controller.Result; // 换成你自己的 Result 类路径
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
public class FileUploadController {

    /**
     * 简单的本地图片上传接口
     */
    @PostMapping("/upload")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        if (file.isEmpty()) {
            return Result.error(400, "上传文件不能为空");
        }

        try {
            // 1. 获取原始文件名和后缀 (如: test.png -> .png)
            String originalFilename = file.getOriginalFilename();
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));

            // 2. 生成一个随机的新文件名，防止名字重复覆盖 (如: uuid.png)
            String newFileName = UUID.randomUUID().toString().replace("-", "") + suffix;

            // 3. 确定保存图片的本地物理路径 (这里保存在项目根目录下的 uploads 文件夹中)
            // 获取项目运行时的根路径
            String projectPath = System.getProperty("user.dir");
            String uploadDir = projectPath + File.separator + "uploads";

            // 如果 uploads 文件夹不存在，则自动创建
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 4. 将文件保存到该物理路径
            File targetFile = new File(uploadDir, newFileName);
            file.transferTo(targetFile);

            // 5. 拼接一个前端可以访问的 URL
            // 这里我们等下需要配置一下 Spring Boot，让它允许通过 URL 访问本地文件夹
            // 假设我们配置的访问前缀是 /images/
            String serverUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
            String imageUrl = serverUrl + "/images/" + newFileName;

            // 返回这个能够被前端 <img> 标签直接解析的 URL
            return Result.success(imageUrl);

        } catch (IOException e) {
            e.printStackTrace();
            return Result.error(500, "文件上传失败：" + e.getMessage());
        }
    }

    /**
     * 🌟 新增：删除本地图片接口
     * 前端传过来的通常是完整的 URL，例如：http://localhost:8080/images/abcd123.png
     * 我们需要把它截取出文件名 abcd123.png，然后在本地删除
     */
    @DeleteMapping("/upload")
    public Result<?> deleteImage(@RequestParam("imageUrl") String imageUrl) {
        System.out.println("收到删除请求，目标URL: " + imageUrl); // 🌟 添加日志

        if (imageUrl == null || imageUrl.isEmpty()) {
            return Result.error(400, "图片URL不能为空");
        }

        try {
            // 1. 稳健地截取文件名 (处理可能存在的路径或参数)
            // 假设 URL 为 http://localhost:8080/images/abc.png
            String path = imageUrl;
            if (path.contains("?")) {
                path = path.substring(0, path.indexOf("?")); // 去掉参数
            }
            String fileName = path.substring(path.lastIndexOf("/") + 1);
            System.out.println("解析出的文件名: " + fileName);

            // 2. 这里的路径必须和上传时的路径完全一致
            String projectPath = System.getProperty("user.dir");
            String uploadDir = projectPath + File.separator + "uploads";

            File targetFile = new File(uploadDir, fileName);
            System.out.println("生成的物理路径: " + targetFile.getAbsolutePath());

            // 3. 执行删除
            if (targetFile.exists()) {
                boolean deleted = targetFile.delete();
                if (deleted) {
                    System.out.println("文件删除成功！");
                    return Result.success("图片删除成功");
                } else {
                    System.err.println("文件删除失败，可能被占用或权限不足");
                    return Result.error(500, "文件被占用，无法删除");
                }
            } else {
                System.err.println("文件不存在，跳过删除步骤");
                return Result.error(404, "文件不存在");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "删除异常: " + e.getMessage());
        }
    }
}
