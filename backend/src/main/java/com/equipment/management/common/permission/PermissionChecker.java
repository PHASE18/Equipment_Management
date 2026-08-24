package com.equipment.management.common.permission;

import com.equipment.management.common.constant.ErrorCode;
import com.equipment.management.common.context.UserContext;
import com.equipment.management.common.exception.BusinessException;
import com.equipment.management.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Set;

@Component
@RequiredArgsConstructor
/** 执行接口权限编码匹配及管理员快速放行判断。 */
public class PermissionChecker {

    private static final Set<String> SUPER_ROLES = Set.of(
            "ADMIN", "SUPER_ADMIN", "SYS_ADMIN", "SYSTEM_ADMIN"
    );

    private final PermissionService permissionService;

    public void check(String... requiredPermissions) { // 检查权限
        if (requiredPermissions == null || requiredPermissions.length == 0) {
            return;
        }

        UserContext.LoginUser user = UserContext.get();// 获取当前用户
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        if (isSuperRole(user)) { // 如果当前用户是超级管理员，则直接返回
            return;
        }

        Set<String> ownedPermissions = resolvePermissions(user); // 获取当前用户的权限
        boolean allowed = Arrays.stream(requiredPermissions) // 检查权限是否匹配
                .filter(StringUtils::hasText)
                .anyMatch(ownedPermissions::contains); // 如果权限匹配，则直接返回
        if (!allowed) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED); // 如果权限不匹配，则抛出异常
        }
    }

    public boolean hasPermission(String permissionCode) { // 检查权限
        if (!StringUtils.hasText(permissionCode)) { // 如果权限码为空，则直接返回 true
            return true;
        }
        UserContext.LoginUser user = UserContext.get(); // 获取当前用户
        if (user == null) {
            return false;
        }
        if (isSuperRole(user)) {
            return true;
        }
        return resolvePermissions(user).contains(permissionCode);
    }

    private Set<String> resolvePermissions(UserContext.LoginUser user) { // 获取当前用户的权限
        if (user.getPermissionCodes() != null && !user.getPermissionCodes().isEmpty()) {
            return user.getPermissionCodes();
        }
        return Set.copyOf(permissionService.getUserPermissionCodes(user.getUserId()));
    }

    private boolean isSuperRole(UserContext.LoginUser user) { // 检查当前用户是否是超级管理员
        if (user.getRoleCodes() == null || user.getRoleCodes().isEmpty()) {
            return false;
        }
        return user.getRoleCodes().stream().anyMatch(SUPER_ROLES::contains);
    }
}
