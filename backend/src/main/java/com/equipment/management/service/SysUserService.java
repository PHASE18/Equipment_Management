package com.equipment.management.service;

import com.equipment.management.common.result.PageResult;
import com.equipment.management.common.service.BaseCrudService;
import com.equipment.management.dto.request.UserQuery;
import com.equipment.management.entity.SysUser;

public interface SysUserService extends BaseCrudService<SysUser> {

    PageResult<SysUser> list(UserQuery query);

    void resetPassword(Long id);
}
