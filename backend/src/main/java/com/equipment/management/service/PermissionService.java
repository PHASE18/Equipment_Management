package com.equipment.management.service;

import com.equipment.management.dto.request.RolePermissionRequest;
import com.equipment.management.dto.request.UserRoleRequest;
import com.equipment.management.dto.response.MenuItemResponse;
import com.equipment.management.entity.SysPermission;

import java.util.List;

/** 权限树、菜单和用户权限计算领域服务。 */
public interface PermissionService {

    List<SysPermission> getMenuTree();

    List<MenuItemResponse> getUserMenus(Long userId);

    List<String> getUserPermissionCodes(Long userId);

    List<SysPermission> getPermissionTree();

    void assignRolePermission(RolePermissionRequest request);

    void assignUserRole(UserRoleRequest request);
}
