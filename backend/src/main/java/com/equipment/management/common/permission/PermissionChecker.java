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
public class PermissionChecker {

    private static final Set<String> SUPER_ROLES = Set.of(
            "ADMIN", "SUPER_ADMIN", "SYS_ADMIN", "SYSTEM_ADMIN"
    );

    private final PermissionService permissionService;

    public void check(String... requiredPermissions) {
        if (requiredPermissions == null || requiredPermissions.length == 0) {
            return;
        }

        UserContext.LoginUser user = UserContext.get();
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        if (isSuperRole(user)) {
            return;
        }

        Set<String> ownedPermissions = resolvePermissions(user);
        boolean allowed = Arrays.stream(requiredPermissions)
                .filter(StringUtils::hasText)
                .anyMatch(ownedPermissions::contains);
        if (!allowed) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }
    }

    public boolean hasPermission(String permissionCode) {
        if (!StringUtils.hasText(permissionCode)) {
            return true;
        }
        UserContext.LoginUser user = UserContext.get();
        if (user == null) {
            return false;
        }
        if (isSuperRole(user)) {
            return true;
        }
        return resolvePermissions(user).contains(permissionCode);
    }

    private Set<String> resolvePermissions(UserContext.LoginUser user) {
        if (user.getPermissionCodes() != null && !user.getPermissionCodes().isEmpty()) {
            return user.getPermissionCodes();
        }
        return Set.copyOf(permissionService.getUserPermissionCodes(user.getUserId()));
    }

    private boolean isSuperRole(UserContext.LoginUser user) {
        if (user.getRoleCodes() == null || user.getRoleCodes().isEmpty()) {
            return false;
        }
        return user.getRoleCodes().stream().anyMatch(SUPER_ROLES::contains);
    }
}
