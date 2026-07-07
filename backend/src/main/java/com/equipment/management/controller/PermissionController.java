package com.equipment.management.controller;

import com.equipment.management.annotation.RequireAuth;
import com.equipment.management.common.result.Result;
import com.equipment.management.dto.request.RolePermissionRequest;
import com.equipment.management.dto.request.UserRoleRequest;
import com.equipment.management.entity.SysPermission;
import com.equipment.management.service.PermissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequireAuth
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping("/api/menu")
    public Result<List<SysPermission>> getMenu() {
        return Result.success(permissionService.getMenuTree());
    }

    @GetMapping("/api/permission")
    public Result<List<SysPermission>> getPermission() {
        return Result.success(permissionService.getPermissionTree());
    }

    @PostMapping("/api/role/permission")
    public Result<Void> assignRolePermission(@Valid @RequestBody RolePermissionRequest request) {
        permissionService.assignRolePermission(request);
        return Result.success();
    }

    @PostMapping("/api/user/role")
    public Result<Void> assignUserRole(@Valid @RequestBody UserRoleRequest request) {
        permissionService.assignUserRole(request);
        return Result.success();
    }
}
