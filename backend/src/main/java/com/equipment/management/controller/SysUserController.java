package com.equipment.management.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.equipment.management.annotation.RequireAuth;
import com.equipment.management.common.controller.BaseCrudController;
import com.equipment.management.common.query.PageQuery;
import com.equipment.management.common.result.PageResult;
import com.equipment.management.common.result.Result;
import com.equipment.management.dto.request.UserQuery;
import com.equipment.management.entity.SysUser;
import com.equipment.management.service.SysUserService;
import jakarta.validation.Valid;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequireAuth
@RequestMapping("/api/user")
public class SysUserController extends BaseCrudController<SysUserService, SysUser> {

    public SysUserController(SysUserService sysUserService) {
        super(sysUserService);
    }

    @GetMapping("/list")
    public Result<PageResult<SysUser>> list(@Valid UserQuery query) {
        return Result.success(baseService.list(query));
    }

    @PutMapping("/resetPassword/{id}")
    public Result<Void> resetPassword(@PathVariable Long id) {
        baseService.resetPassword(id);
        return Result.success();
    }

    @Override
    protected QueryWrapper<SysUser> buildQueryWrapper(PageQuery query) {
        QueryWrapper<SysUser> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like("username", query.getKeyword())
                    .or().like("real_name", query.getKeyword()));
        }
        wrapper.orderByDesc("create_time");
        return wrapper;
    }
}
