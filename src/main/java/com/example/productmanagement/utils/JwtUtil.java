package com.example.productmanagement.utils;

import com.example.productmanagement.exception.BizIllegalException;
import com.example.productmanagement.result.ResultCodeEnum;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;

public class JwtUtil {

    private static final long TOKEN_EXPIRATION =  2*60*60 * 1000L; // 2小时

    private static final SecretKey SECRET_KEY =
            Keys.hmacShaKeyFor("gBHACI6DFjU1dy0YDkEBq1POu38Vvo34".getBytes());

    /**
     * 创建 JWT，将 userId 和 role 写入载荷。
     *
     * @param userId 用户主键 ID
     * @param role   用户角色（1-普通用户，2-管理员）
     * @return 签名后的 JWT 字符串
     */
    public static String createJwt(Long userId, Integer role) {
        return Jwts.builder()
                .setExpiration(new Date(System.currentTimeMillis() + TOKEN_EXPIRATION))
                .setSubject("LOGIN_USER")
                .claim("userId", String.valueOf(userId))
                .claim("role", role)
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 解析 JWT，返回 Claims 载荷。
     *
     * @param token JWT 字符串（从请求头获取）
     * @return Claims 对象，可通过 {@code get("userId", Long.class)} 等方式取值
     * @throws RuntimeException token 为空、过期或非法时抛出
     */
    public static Claims parseToken(String token) {
        if (token == null) {
            throw new BizIllegalException(ResultCodeEnum.LOGIN_AUTH);
        }
        try {
            JwtParser jwtParser = Jwts.parserBuilder().setSigningKey(SECRET_KEY).build();
            Jws<Claims> claimsJws = jwtParser.parseClaimsJws(token);
            return claimsJws.getBody();
        } catch (ExpiredJwtException e) {
            throw new BizIllegalException(ResultCodeEnum.TOKEN_EXPIRED);
        } catch (JwtException e) {
            throw new BizIllegalException(ResultCodeEnum.TOKEN_INVALID);
        }
    }
}