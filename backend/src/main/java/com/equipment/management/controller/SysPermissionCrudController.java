package com.equipment.management.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.equipment.management.annotation.RequireAuth;
import com.equipment.management.common.controller.BaseCrudController;
import com.equipment.management.common.query.PageQuery;
import com.equipment.management.entity.SysPermission;
import com.equipment.management.service.SysPermissionCrudService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequireAuth
@RequestMapping("/api/permission/crud")
public class SysPermissionCrudController extends BaseCrudController<SysPermissionCrudService, SysPermission> {

    public SysPermissionCrudController(SysPermissionCrudService sysPermissionCrudService) {
        super(sysPermissionCrudService);
    }

    @Override
    protected QueryWrapper<SysPermission> buildQueryWrapper(PageQuery query) {
        QueryWrapper<SysPermission> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like("permission_name", query.getKeyword())
                    .or().like("permission_code", query.getKeyword()));
        }
        wrapper.orderByAsc("sort").orderByDesc("create_time");
        return wrapper;
    }
}
