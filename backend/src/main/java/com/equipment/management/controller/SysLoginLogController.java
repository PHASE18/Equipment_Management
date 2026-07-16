package com.equipment.management.controller;

import com.equipment.management.annotation.RequireAuth;
import com.equipment.management.annotation.RequirePermission;
import com.equipment.management.common.result.PageResult;
import com.equipment.management.common.result.Result;
import com.equipment.management.dto.request.LogQuery;
import com.equipment.management.entity.SysLoginLog;
import com.equipment.management.service.SysLoginLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequireAuth
@RequirePermission(any = {"log:view", "log:list"})
@RequestMapping("/api/login-log")
@RequiredArgsConstructor
public class SysLoginLogController {

    private final SysLoginLogService sysLoginLogService;

    @GetMapping("/page")
    public Result<PageResult<SysLoginLog>> page(@Valid LogQuery query) {
        return Result.success(sysLoginLogService.pageQuery(query));
    }

    @GetMapping("/{id}")
    public Result<SysLoginLog> getById(@PathVariable Long id) {
        return Result.success(sysLoginLogService.getDetail(id));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        sysLoginLogService.removeLog(id);
        return Result.success();
    }
}
