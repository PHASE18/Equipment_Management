package com.equipment.management.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.equipment.management.common.query.PageQuery;
import com.equipment.management.common.result.PageResult;
import com.equipment.management.dto.request.UserRoleRequest;
import com.equipment.management.entity.SysUserRole;

public interface SysUserRoleService extends IService<SysUserRole> {

    PageResult<SysUserRole> pageQuery(PageQuery query, Long userId, Long roleId);

    void bind(UserRoleRequest request);

    void unbind(UserRoleRequest request);
}
