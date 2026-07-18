package com.equipment.management.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.equipment.management.annotation.CrudPermission;
import com.equipment.management.annotation.RequireAuth;
import com.equipment.management.common.controller.BaseCrudController;
import com.equipment.management.common.query.PageQuery;
import com.equipment.management.common.result.Result;
import com.equipment.management.entity.SysDepartment;
import com.equipment.management.service.SysDepartmentService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequireAuth
@CrudPermission(module = "system:dept")
@RequestMapping("/api/department")
/** 组织部门树管理接口。 */
public class SysDepartmentController extends BaseCrudController<SysDepartmentService, SysDepartment> {

    public SysDepartmentController(SysDepartmentService sysDepartmentService) {
        super(sysDepartmentService);
    }

    @GetMapping("/tree")
    public Result<List<SysDepartment>> tree() {
        return Result.success(baseService.tree());
    }

    @Override
    protected QueryWrapper<SysDepartment> buildQueryWrapper(PageQuery query) {
        QueryWrapper<SysDepartment> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.like("department_name", query.getKeyword());
        }
        wrapper.orderByAsc("parent_id").orderByAsc("id");
        return wrapper;
    }
}
