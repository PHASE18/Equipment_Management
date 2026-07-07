package com.equipment.management.controller;

import com.equipment.management.annotation.RequireAuth;
import com.equipment.management.common.query.PageQuery;
import com.equipment.management.common.result.PageResult;
import com.equipment.management.common.result.Result;
import com.equipment.management.dto.request.RolePermissionRequest;
import com.equipment.management.entity.SysRolePermission;
import com.equipment.management.service.SysRolePermissionService;
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
@RequestMapping("/api/role-permission")
@RequiredArgsConstructor
public class SysRolePermissionController {

    private final SysRolePermissionService sysRolePermissionService;

    @GetMapping("/page")
    public Result<PageResult<SysRolePermission>> page(@Valid PageQuery query,
                                                      @RequestParam(required = false) Long roleId,
                                                      @RequestParam(required = false) Long permissionId) {
        return Result.success(sysRolePermissionService.pageQuery(query, roleId, permissionId));
    }

    @PostMapping("/bind")
    public Result<Void> bind(@Valid @RequestBody RolePermissionRequest request) {
        sysRolePermissionService.bind(request);
        return Result.success();
    }

    @DeleteMapping("/unbind")
    public Result<Void> unbind(@Valid @RequestBody RolePermissionRequest request) {
        sysRolePermissionService.unbind(request);
        return Result.success();
    }
}
