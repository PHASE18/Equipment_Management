package com.equipment.management.service.impl;

import com.equipment.management.entity.SysPermission;
import com.equipment.management.mapper.SysPermissionMapper;
import com.equipment.management.common.service.impl.BaseCrudServiceImpl;
import com.equipment.management.service.SysPermissionCrudService;
import org.springframework.stereotype.Service;

@Service
public class SysPermissionCrudServiceImpl extends BaseCrudServiceImpl<SysPermissionMapper, SysPermission>
        implements SysPermissionCrudService {
}
