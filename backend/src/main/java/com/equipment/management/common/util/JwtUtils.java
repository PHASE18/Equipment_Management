package com.equipment.management.common.util;

import com.equipment.management.common.context.UserContext;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * JWT 令牌工具，负责令牌签发、签名校验、有限时间内的过期令牌解析，
 * 以及令牌声明与当前登录用户对象之间的转换。
 */
@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    @Value("${jwt.refresh-grace-period:604800000}")
    private long refreshGracePeriod;

    /** 根据登录用户信息创建带签名和过期时间的访问令牌。 */
    public String generateToken(UserContext.LoginUser user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expiration);
        return Jwts.builder()
                .subject(String.valueOf(user.getUserId()))
                .claim("username", user.getUsername())
                .claim("realName", user.getRealName())
                .claim("departmentId", user.getDepartmentId())
                .claim("roleCodes", String.join(",", user.getRoleCodes()))
                .claim("dataScope", user.getDataScope().name())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSignKey())
                .compact();
    }

    /** 校验令牌签名并解析未过期令牌的声明。 */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** 在刷新宽限期内允许读取过期令牌，超出宽限期仍抛出异常。 */
    public Claims parseTokenAllowExpired(String token) {
        try {
            return parseToken(token);
        } catch (ExpiredJwtException e) {
            Date expiredAt = e.getClaims().getExpiration();
            if (expiredAt == null || System.currentTimeMillis() - expiredAt.getTime() > refreshGracePeriod) {
                throw e;
            }
            return e.getClaims();
        }
    }

    /** 将 JWT 声明恢复为请求上下文使用的登录用户对象。 */
    public UserContext.LoginUser toLoginUser(Claims claims) {
        UserContext.LoginUser user = new UserContext.LoginUser();
        user.setUserId(Long.parseLong(claims.getSubject()));
        user.setUsername(claims.get("username", String.class));
        user.setRealName(claims.get("realName", String.class));
        user.setDepartmentId(claims.get("departmentId", Long.class));
        user.setRoleCodes(parseRoleCodes(claims.get("roleCodes", String.class)));
        String dataScope = claims.get("dataScope", String.class);
        if (dataScope != null) {
            user.setDataScope(UserContext.DataScope.valueOf(dataScope));
        }
        return user;
    }

    /** 判断令牌的过期时间是否早于当前时间。 */
    public boolean isTokenExpired(String token) {
        return parseToken(token).getExpiration().before(new Date());
    }

    /** 将逗号分隔的角色编码清洗并转换为集合。 */
    private Set<String> parseRoleCodes(String roleCodes) {
        if (roleCodes == null || roleCodes.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(roleCodes.split(","))
                .map(String::trim)
                .filter(role -> !role.isEmpty())
                .collect(Collectors.toSet());
    }

    /** 使用配置的密钥生成 HMAC 签名密钥。 */
    private SecretKey getSignKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
