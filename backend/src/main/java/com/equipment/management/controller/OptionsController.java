package com.equipment.management.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.equipment.management.annotation.RequireAuth;
import com.equipment.management.common.constant.DictTypeConstants;
import com.equipment.management.common.result.Result;
import com.equipment.management.dto.response.UserOptionResponse;
import com.equipment.management.entity.SysDepartment;
import com.equipment.management.entity.SysDict;
import com.equipment.management.entity.SysUser;
import com.equipment.management.service.DictService;
import com.equipment.management.service.SysDepartmentService;
import com.equipment.management.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 业务页只读选项接口：登录即可访问，不占用 system:* 管理权限。
 * 用于设备/首页/项目等页面的下拉与筛选，与系统管理 CRUD 分离。
 */
@RestController
@RequireAuth
@RequestMapping("/api/options")
@RequiredArgsConstructor
public class OptionsController {

    private final SysDepartmentService departmentService;
    private final DictService dictService;
    private final SysUserService userService;

    @GetMapping("/departments")
    public Result<List<SysDepartment>> departments() {
        return Result.success(departmentService.tree());
    }

    @GetMapping("/brands")
    public Result<List<SysDict>> brands() {
        return Result.success(dictService.listByType(DictTypeConstants.DEVICE_BRAND));
    }

    @GetMapping("/device-types")
    public Result<List<SysDict>> deviceTypes() {
        return Result.success(dictService.listByType(DictTypeConstants.DEVICE_TYPE));
    }

    @GetMapping("/users")
    public Result<List<UserOptionResponse>> users() {
        List<SysUser> users = userService.list(Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getStatus, 1)
                .orderByAsc(SysUser::getId));
        List<UserOptionResponse> options = users.stream()
                .map(user -> UserOptionResponse.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .realName(user.getRealName())
                        .departmentId(user.getDepartmentId())
                        .status(user.getStatus())
                        .build())
                .toList();
        return Result.success(options);
    }
}
