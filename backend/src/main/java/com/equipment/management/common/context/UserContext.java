package com.equipment.management.common.context;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

/**
 * 当前登录用户上下文（由 JWT 拦截器填充）
 */
public final class UserContext {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    public static LoginUser get() {
        return HOLDER.get();
    }

    public static Long getUserId() {
        LoginUser user = get();
        return user != null ? user.getUserId() : null;
    }

    public static String getUsername() {
        LoginUser user = get();
        return user != null ? user.getUsername() : null;
    }

    public static Long getDepartmentId() {
        LoginUser user = get();
        return user != null ? user.getDepartmentId() : null;
    }

    public static void clear() {
        HOLDER.remove();
    }

    @Data
    public static class LoginUser {
        private Long userId;
        private String username;
        private String realName;
        private Long departmentId;
        private Set<String> roleCodes = new HashSet<>();
        private Set<String> permissionCodes = new HashSet<>();
        private DataScope dataScope = DataScope.SELF;

        public boolean hasRole(String roleCode) {
            return roleCode != null && roleCodes != null && roleCodes.contains(roleCode);
        }

        public boolean hasPermission(String permissionCode) {
            return permissionCode != null && permissionCodes != null && permissionCodes.contains(permissionCode);
        }

        public boolean isAllDataScope() {
            return DataScope.ALL.equals(dataScope);
        }

        public boolean isDepartmentDataScope() {
            return DataScope.DEPARTMENT.equals(dataScope);
        }
    }

    public enum DataScope {
        ALL,
        DEPARTMENT,
        SELF
    }
}
