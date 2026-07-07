package com.equipment.management.service.impl;

import com.equipment.management.dto.request.RolePermissionRequest;
import com.equipment.management.dto.request.UserRoleRequest;
import com.equipment.management.entity.SysPermission;
import com.equipment.management.service.PermissionService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class PermissionServiceImpl implements PermissionService {

    @Override
    public List<SysPermission> getMenuTree() {
        // TODO: 根据当前用户角色返回菜单树
        return Collections.emptyList();
    }

    @Override
    public List<SysPermission> getPermissionTree() {
        // TODO: 返回完整权限树（管理员）
        return Collections.emptyList();
    }

    @Override
    public void assignRolePermission(RolePermissionRequest request) {
        // TODO: 更新 sys_role_permission
    }

    @Override
    public void assignUserRole(UserRoleRequest request) {
        // TODO: 更新 sys_user_role
    }
}
