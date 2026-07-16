package com.equipment.management.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.equipment.management.common.query.PageQuery;
import com.equipment.management.common.result.PageResult;
import com.equipment.management.common.util.PageUtils;
import com.equipment.management.dto.request.UserRoleRequest;
import com.equipment.management.entity.SysUserRole;
import com.equipment.management.mapper.SysUserRoleMapper;
import com.equipment.management.service.SysUserRoleService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SysUserRoleServiceImpl extends ServiceImpl<SysUserRoleMapper, SysUserRole> implements SysUserRoleService {

    @Override
    public PageResult<SysUserRole> pageQuery(PageQuery query, Long userId, Long roleId) {
        LambdaQueryWrapper<SysUserRole> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(userId != null, SysUserRole::getUserId, userId)
                .eq(roleId != null, SysUserRole::getRoleId, roleId)
                .orderByDesc(SysUserRole::getCreateTime);
        return PageUtils.toPageResult(page(PageUtils.buildPage(query), wrapper));
    }

    @Override
    public List<Long> listRoleIdsByUserId(Long userId) {
        return list(Wrappers.<SysUserRole>lambdaQuery()
                .eq(SysUserRole::getUserId, userId))
                .stream()
                .map(SysUserRole::getRoleId)
                .toList();
    }

    @Override
    public void bind(UserRoleRequest request) {
        for (Long roleId : request.getRoleIds()) {
            long count = count(Wrappers.<SysUserRole>lambdaQuery()
                    .eq(SysUserRole::getUserId, request.getUserId())
                    .eq(SysUserRole::getRoleId, roleId));
            if (count == 0) {
                SysUserRole userRole = new SysUserRole();
                userRole.setUserId(request.getUserId());
                userRole.setRoleId(roleId);
                userRole.setCreateTime(LocalDateTime.now());
                save(userRole);
            }
        }
    }

    @Override
    public void unbind(UserRoleRequest request) {
        remove(Wrappers.<SysUserRole>lambdaQuery()
                .eq(SysUserRole::getUserId, request.getUserId())
                .in(SysUserRole::getRoleId, request.getRoleIds()));
    }
}
