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
/** 根据控制器权限注解和当前用户权限判断接口是否允许执行。 */
public class PermissionInterceptor implements HandlerInterceptor {

    private final PermissionChecker permissionChecker; // 权限检查器

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) { // 如果请求方法为 OPTIONS，则直接返回 true
            return true;
        }
        if (!(handler instanceof HandlerMethod handlerMethod)) { // 如果 handler 不是 HandlerMethod，则直接返回 true
            return true;
        }
        if (isAnonymous(handlerMethod)) { // 如果方法上标注了 Anonymous 注解，则直接返回 true
            return true;
        }

        String[] requiredPermissions = resolveRequiredPermissions(handlerMethod, request.getMethod()); // 解析需要的权限
        if (requiredPermissions.length == 0) {
            return true;
        }

        permissionChecker.check(requiredPermissions); // 检查权限
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
