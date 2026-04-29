package com.example.productmanagement.utils;

import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;

/**
 * RSA 非对称加密工具类（基于 Hutool）
 *
 * <p>使用说明：
 * <ul>
 *   <li>前端使用 {@link #getPublicKeyBase64()} 获取公钥，在客户端对明文密码进行加密后传输。</li>
 *   <li>后端调用 {@link #decrypt(String)} 使用私钥解密，得到原始密码明文，再进行业务验证。</li>
 *   <li>密钥对在类加载时一次性生成并保存在内存中；生产环境建议将密钥持久化到配置文件或密钥管理服务。</li>
 * </ul>
 */
public class RsaCryptoUtil {

    /** Hutool RSA 实例（内部持有公私钥对） */
    private static final RSA RSA_INSTANCE;

    static {
        // 无参构造会自动生成一对 RSA 密钥（2048 位）
        RSA_INSTANCE = new RSA();
    }

    /**
     * 获取 Base64 编码的公钥字符串，供前端加密使用。
     *
     * @return Base64 公钥
     */
    public static String getPublicKeyBase64() {
        return RSA_INSTANCE.getPublicKeyBase64();
    }

    /**
     * 获取 Base64 编码的私钥字符串（仅供内部使用，不要对外暴露）。
     *
     * @return Base64 私钥
     */
    public static String getPrivateKeyBase64() {
        return RSA_INSTANCE.getPrivateKeyBase64();
    }

    /**
     * 使用公钥对明文进行加密，返回 Base64 密文。
     *
     * @param plainText 明文字符串
     * @return Base64 密文
     */
    public static String encrypt(String plainText) {
        return RSA_INSTANCE.encryptBase64(plainText, KeyType.PublicKey);
    }

    /**
     * 使用私钥对 Base64 密文进行解密，返回原始明文。
     *
     * @param cipherTextBase64 Base64 密文（由公钥加密得到）
     * @return 解密后的明文字符串
     * @throws cn.hutool.core.exceptions.UtilException 解密失败时抛出
     */
    public static String decrypt(String cipherTextBase64) {
        return RSA_INSTANCE.decryptStr(cipherTextBase64, KeyType.PrivateKey);
    }

    public static void main(String[] args) {
        String se_pass = encrypt("750301");
        System.out.println(se_pass);
        String decrypt = decrypt(se_pass);
        System.out.println(decrypt);

    }
}
