package com.equipment.management.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.equipment.management.common.query.PageQuery;
import com.equipment.management.common.result.PageResult;
import com.equipment.management.common.util.PageUtils;
import com.equipment.management.dto.request.RolePermissionRequest;
import com.equipment.management.entity.SysRolePermission;
import com.equipment.management.mapper.SysRolePermissionMapper;
import com.equipment.management.service.SysRolePermissionService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SysRolePermissionServiceImpl extends ServiceImpl<SysRolePermissionMapper, SysRolePermission>
        implements SysRolePermissionService {

    @Override
    public PageResult<SysRolePermission> pageQuery(PageQuery query, Long roleId, Long permissionId) {
        LambdaQueryWrapper<SysRolePermission> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(roleId != null, SysRolePermission::getRoleId, roleId)
                .eq(permissionId != null, SysRolePermission::getPermissionId, permissionId)
                .orderByDesc(SysRolePermission::getCreateTime);
        return PageUtils.toPageResult(page(PageUtils.buildPage(query), wrapper));
    }

    @Override
    public void bind(RolePermissionRequest request) {
        for (Long permissionId : request.getPermissionIds()) {
            long count = count(Wrappers.<SysRolePermission>lambdaQuery()
                    .eq(SysRolePermission::getRoleId, request.getRoleId())
                    .eq(SysRolePermission::getPermissionId, permissionId));
            if (count == 0) {
                SysRolePermission rolePermission = new SysRolePermission();
                rolePermission.setRoleId(request.getRoleId());
                rolePermission.setPermissionId(permissionId);
                rolePermission.setCreateTime(LocalDateTime.now());
                save(rolePermission);
            }
        }
    }

    @Override
    public void unbind(RolePermissionRequest request) {
        remove(Wrappers.<SysRolePermission>lambdaQuery()
                .eq(SysRolePermission::getRoleId, request.getRoleId())
                .in(SysRolePermission::getPermissionId, request.getPermissionIds()));
    }
}
