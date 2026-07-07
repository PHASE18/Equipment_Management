package com.equipment.management.service;

import com.equipment.management.dto.request.RolePermissionRequest;
import com.equipment.management.dto.request.UserRoleRequest;
import com.equipment.management.entity.SysPermission;

import java.util.List;

public interface PermissionService {

    List<SysPermission> getMenuTree();

    List<SysPermission> getPermissionTree();

    void assignRolePermission(RolePermissionRequest request);

    void assignUserRole(UserRoleRequest request);
}
