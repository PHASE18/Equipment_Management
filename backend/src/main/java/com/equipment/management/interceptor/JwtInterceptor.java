package com.equipment.management.interceptor;

import com.equipment.management.annotation.AllowExpiredToken;
import com.equipment.management.annotation.Anonymous;
import com.equipment.management.common.constant.ErrorCode;
import com.equipment.management.common.context.UserContext;
import com.equipment.management.common.exception.BusinessException;
import com.equipment.management.common.util.JwtUtils;
import com.equipment.management.service.PermissionService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

/**
 * JWT 统一鉴权拦截器，配合 {@link Anonymous} / {@link com.equipment.management.annotation.RequireAuth} 注解
 */
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtils jwtUtils;
    private final PermissionService permissionService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        if (isAnonymous(handler)) {
            return true;
        }

        String token = resolveToken(request);
        if (!StringUtils.hasText(token)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        try {
            Claims claims = allowExpiredToken(handler)
                    ? jwtUtils.parseTokenAllowExpired(token)
                    : jwtUtils.parseToken(token);
            UserContext.LoginUser loginUser = jwtUtils.toLoginUser(claims);
            loginUser.setPermissionCodes(Set.copyOf(permissionService.getUserPermissionCodes(loginUser.getUserId())));
            UserContext.set(loginUser);
            return true;
        } catch (ExpiredJwtException e) {
            throw new BusinessException(ErrorCode.TOKEN_EXPIRED);
        } catch (JwtException e) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        UserContext.clear();
    }

    private boolean isAnonymous(Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return false;
        }
        if (handlerMethod.hasMethodAnnotation(Anonymous.class)) {
            return true;
        }
        return handlerMethod.getBeanType().isAnnotationPresent(Anonymous.class);
    }

    private boolean allowExpiredToken(Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return false;
        }
        return handlerMethod.hasMethodAnnotation(AllowExpiredToken.class);
    }

    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader(AUTH_HEADER);
        if (StringUtils.hasText(header) && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
