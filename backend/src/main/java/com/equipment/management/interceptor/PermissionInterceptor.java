package com.equipment.management.interceptor;

import com.equipment.management.annotation.Anonymous;
import com.equipment.management.annotation.CrudPermission;
import com.equipment.management.annotation.RequirePermission;
import com.equipment.management.common.permission.PermissionChecker;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class PermissionInterceptor implements HandlerInterceptor {

    private final PermissionChecker permissionChecker;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        if (isAnonymous(handlerMethod)) {
            return true;
        }

        String[] requiredPermissions = resolveRequiredPermissions(handlerMethod, request.getMethod());
        if (requiredPermissions.length == 0) {
            return true;
        }

        permissionChecker.check(requiredPermissions);
        return true;
    }

    private String[] resolveRequiredPermissions(HandlerMethod handlerMethod, String httpMethod) {
        RequirePermission methodPermission = handlerMethod.getMethodAnnotation(RequirePermission.class);
        if (methodPermission != null) {
            return toPermissionArray(methodPermission);
        }

        RequirePermission classPermission = handlerMethod.getBeanType().getAnnotation(RequirePermission.class);
        if (classPermission != null) {
            return toPermissionArray(classPermission);
        }

        CrudPermission crudPermission = handlerMethod.getBeanType().getAnnotation(CrudPermission.class);
        if (crudPermission != null) {
            String suffix = resolveCrudSuffix(handlerMethod.getMethod().getName(), httpMethod);
            if (suffix != null) {
                return new String[]{crudPermission.module() + ":" + suffix};
            }
        }
        return new String[0];
    }

    private String[] toPermissionArray(RequirePermission permission) {
        if (permission.any().length > 0) {
            return permission.any();
        }
        if (StringUtils.hasText(permission.value())) {
            return new String[]{permission.value()};
        }
        return new String[0];
    }

    private String resolveCrudSuffix(String methodName, String httpMethod) {
        return switch (methodName) {
            case "list", "page", "tree", "home", "brand", "type", "status", "fault", "faultRank", "cost" -> "list";
            case "getById", "getDetail" -> "view";
            case "create", "createEntity", "bind", "bindDevice", "change", "upload" -> "add";
            case "update", "updateEntity" -> "edit";
            case "delete", "removeEntity", "removeLog", "unbind" -> "delete";
            default -> switch (httpMethod) {
                case "GET" -> "list";
                case "POST" -> "add";
                case "PUT" -> "edit";
                case "DELETE" -> "delete";
                default -> null;
            };
        };
    }

    private boolean isAnonymous(HandlerMethod handlerMethod) {
        if (handlerMethod.hasMethodAnnotation(Anonymous.class)) {
            return true;
        }
        return handlerMethod.getBeanType().isAnnotationPresent(Anonymous.class);
    }
}
