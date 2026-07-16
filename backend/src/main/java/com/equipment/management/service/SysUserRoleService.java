package com.equipment.management.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.equipment.management.common.query.PageQuery;
import com.equipment.management.common.result.PageResult;
import com.equipment.management.dto.request.UserRoleRequest;
import com.equipment.management.entity.SysUserRole;

import java.util.List;

public interface SysUserRoleService extends IService<SysUserRole> {

    PageResult<SysUserRole> pageQuery(PageQuery query, Long userId, Long roleId);

    List<Long> listRoleIdsByUserId(Long userId);

    void bind(UserRoleRequest request);

    void unbind(UserRoleRequest request);
}
