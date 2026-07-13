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

@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    @Value("${jwt.refresh-grace-period:604800000}")
    private long refreshGracePeriod;

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

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

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

    public boolean isTokenExpired(String token) {
        return parseToken(token).getExpiration().before(new Date());
    }

    private Set<String> parseRoleCodes(String roleCodes) {
        if (roleCodes == null || roleCodes.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(roleCodes.split(","))
                .map(String::trim)
                .filter(role -> !role.isEmpty())
                .collect(Collectors.toSet());
    }

    private SecretKey getSignKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
