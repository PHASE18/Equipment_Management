package com.equipment.management.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.equipment.management.annotation.CrudPermission;
import com.equipment.management.annotation.RequireAuth;
import com.equipment.management.common.controller.BaseCrudController;
import com.equipment.management.common.query.PageQuery;
import com.equipment.management.entity.SysRole;
import com.equipment.management.service.SysRoleService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequireAuth
@CrudPermission(module = "system:role")
@RequestMapping("/api/role")
/** 系统角色管理和角色权限接口。 */
public class SysRoleController extends BaseCrudController<SysRoleService, SysRole> {

    public SysRoleController(SysRoleService sysRoleService) {
        super(sysRoleService);
    }

    @Override
    protected QueryWrapper<SysRole> buildQueryWrapper(PageQuery query) {
        QueryWrapper<SysRole> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like("role_name", query.getKeyword())
                    .or().like("role_code", query.getKeyword()));
        }
        wrapper.orderByDesc("create_time");
        return wrapper;
    }
}
