package com.equipment.management.controller;

import com.equipment.management.annotation.RequireAuth;
import com.equipment.management.common.query.PageQuery;
import com.equipment.management.common.result.PageResult;
import com.equipment.management.common.result.Result;
import com.equipment.management.dto.request.UserRoleRequest;
import com.equipment.management.entity.SysUserRole;
import com.equipment.management.service.SysUserRoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequireAuth
@RequestMapping("/api/user-role")
@RequiredArgsConstructor
public class SysUserRoleController {

    private final SysUserRoleService sysUserRoleService;

    @GetMapping("/page")
    public Result<PageResult<SysUserRole>> page(@Valid PageQuery query,
                                                @RequestParam(required = false) Long userId,
                                                @RequestParam(required = false) Long roleId) {
        return Result.success(sysUserRoleService.pageQuery(query, userId, roleId));
    }

    @PostMapping("/bind")
    public Result<Void> bind(@Valid @RequestBody UserRoleRequest request) {
        sysUserRoleService.bind(request);
        return Result.success();
    }

    @DeleteMapping("/unbind")
    public Result<Void> unbind(@Valid @RequestBody UserRoleRequest request) {
        sysUserRoleService.unbind(request);
        return Result.success();
    }
}
