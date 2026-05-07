package com.example.productmanagement.controller.auth;



import com.example.productmanagement.result.Result;
import com.example.productmanagement.utils.RsaCryptoUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * RSA 公钥控制器（公开接口，无需认证）
 * 
 * <p>前端在登录/注册前调用此接口获取 RSA 公钥，用于加密密码。
 */
@RestController
@RequestMapping("/api/auth")
public class PublicKeyController {

    /**
     * 获取 RSA 公钥
     * 
     * @return Base64 编码的公钥字符串
     */
    @GetMapping("/public-key")
    public Result<Map<String, String>> getPublicKey() {
        Map<String, String> result = new HashMap<>();
        result.put("publicKey", RsaCryptoUtil.getPublicKeyBase64());
        return Result.ok(result);
    }
}
