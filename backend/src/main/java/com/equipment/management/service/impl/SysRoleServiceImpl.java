package com.equipment.management.service.impl;

import com.equipment.management.entity.SysRole;
import com.equipment.management.mapper.SysRoleMapper;
import com.equipment.management.common.service.impl.BaseCrudServiceImpl;
import com.equipment.management.service.SysRoleService;
import org.springframework.stereotype.Service;

@Service
public class SysRoleServiceImpl extends BaseCrudServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {
}
