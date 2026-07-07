package com.equipment.management.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.equipment.management.common.query.PageQuery;
import com.equipment.management.common.result.PageResult;
import com.equipment.management.dto.request.RolePermissionRequest;
import com.equipment.management.entity.SysRolePermission;

public interface SysRolePermissionService extends IService<SysRolePermission> {

    PageResult<SysRolePermission> pageQuery(PageQuery query, Long roleId, Long permissionId);

    void bind(RolePermissionRequest request);

    void unbind(RolePermissionRequest request);
}
