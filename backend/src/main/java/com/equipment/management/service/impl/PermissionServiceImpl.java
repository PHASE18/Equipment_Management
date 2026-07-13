package com.equipment.management.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.equipment.management.common.context.UserContext;
import com.equipment.management.dto.request.RolePermissionRequest;
import com.equipment.management.dto.request.UserRoleRequest;
import com.equipment.management.dto.response.MenuItemResponse;
import com.equipment.management.entity.SysPermission;
import com.equipment.management.entity.SysRolePermission;
import com.equipment.management.entity.SysUserRole;
import com.equipment.management.mapper.SysPermissionMapper;
import com.equipment.management.mapper.SysRolePermissionMapper;
import com.equipment.management.mapper.SysUserRoleMapper;
import com.equipment.management.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private static final int MENU_TYPE = 1;

    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRolePermissionMapper sysRolePermissionMapper;
    private final SysPermissionMapper sysPermissionMapper;

    @Override
    public List<SysPermission> getMenuTree() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Collections.emptyList();
        }
        return toSysPermissionTree(getUserMenus(userId));
    }

    @Override
    public List<MenuItemResponse> getUserMenus(Long userId) {
        List<SysPermission> menus = loadUserPermissions(userId, MENU_TYPE);
        return buildMenuTree(menus, 0L);
    }

    @Override
    public List<String> getUserPermissionCodes(Long userId) {
        return loadUserPermissions(userId, null).stream()
                .map(SysPermission::getPermissionCode)
                .distinct()
                .sorted()
                .toList();
    }

    @Override
    public List<SysPermission> getPermissionTree() {
        List<SysPermission> permissions = sysPermissionMapper.selectList(Wrappers.<SysPermission>lambdaQuery()
                .eq(SysPermission::getStatus, 1)
                .orderByAsc(SysPermission::getSort));
        return buildSysPermissionTree(permissions, 0L);
    }

    @Override
    public void assignRolePermission(RolePermissionRequest request) {
        // TODO: 更新 sys_role_permission
    }

    @Override
    public void assignUserRole(UserRoleRequest request) {
        // TODO: 更新 sys_user_role
    }

    private List<SysPermission> loadUserPermissions(Long userId, Integer permissionType) {
        List<Long> roleIds = sysUserRoleMapper.selectList(Wrappers.<SysUserRole>lambdaQuery()
                        .eq(SysUserRole::getUserId, userId))
                .stream()
                .map(SysUserRole::getRoleId)
                .toList();
        if (roleIds.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> permissionIds = sysRolePermissionMapper.selectList(Wrappers.<SysRolePermission>lambdaQuery()
                        .in(SysRolePermission::getRoleId, roleIds))
                .stream()
                .map(SysRolePermission::getPermissionId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (permissionIds.isEmpty()) {
            return Collections.emptyList();
        }

        return sysPermissionMapper.selectList(Wrappers.<SysPermission>lambdaQuery()
                        .in(SysPermission::getId, permissionIds)
                        .eq(permissionType != null, SysPermission::getPermissionType, permissionType)
                        .eq(SysPermission::getStatus, 1)
                        .orderByAsc(SysPermission::getSort));
    }

    private List<MenuItemResponse> buildMenuTree(List<SysPermission> menus, Long parentId) {
        return menus.stream()
                .filter(menu -> Objects.equals(menu.getParentId(), parentId))
                .sorted(Comparator.comparing(SysPermission::getSort, Comparator.nullsLast(Integer::compareTo)))
                .map(menu -> MenuItemResponse.builder()
                        .id(menu.getId())
                        .parentId(menu.getParentId())
                        .title(menu.getPermissionName())
                        .path(menu.getPath())
                        .permissionCode(menu.getPermissionCode())
                        .icon(menu.getIcon())
                        .children(buildMenuTree(menus, menu.getId()))
                        .build())
                .toList();
    }

    private List<SysPermission> buildSysPermissionTree(List<SysPermission> permissions, Long parentId) {
        return permissions.stream()
                .filter(permission -> Objects.equals(permission.getParentId(), parentId))
                .sorted(Comparator.comparing(SysPermission::getSort, Comparator.nullsLast(Integer::compareTo)))
                .peek(permission -> permission.setChildren(buildSysPermissionTree(permissions, permission.getId())))
                .toList();
    }

    private List<SysPermission> toSysPermissionTree(List<MenuItemResponse> menus) {
        List<SysPermission> result = new ArrayList<>();
        for (MenuItemResponse menu : menus) {
            SysPermission permission = new SysPermission();
            permission.setId(menu.getId());
            permission.setParentId(menu.getParentId());
            permission.setPermissionName(menu.getTitle());
            permission.setPermissionCode(menu.getPermissionCode());
            permission.setPath(menu.getPath());
            permission.setIcon(menu.getIcon());
            permission.setChildren(toSysPermissionTree(menu.getChildren() != null ? menu.getChildren() : List.of()));
            result.add(permission);
        }
        return result;
    }
}
