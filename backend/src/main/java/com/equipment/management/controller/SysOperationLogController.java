package com.equipment.management.controller;

import com.equipment.management.annotation.RequireAuth;
import com.equipment.management.common.result.PageResult;
import com.equipment.management.common.result.Result;
import com.equipment.management.dto.request.LogQuery;
import com.equipment.management.entity.SysOperationLog;
import com.equipment.management.service.SysOperationLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequireAuth
@RequestMapping("/api/operation-log")
@RequiredArgsConstructor
public class SysOperationLogController {

    private final SysOperationLogService sysOperationLogService;

    @GetMapping("/page")
    public Result<PageResult<SysOperationLog>> page(@Valid LogQuery query) {
        return Result.success(sysOperationLogService.pageQuery(query));
    }

    @GetMapping("/{id}")
    public Result<SysOperationLog> getById(@PathVariable Long id) {
        return Result.success(sysOperationLogService.getDetail(id));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        sysOperationLogService.removeLog(id);
        return Result.success();
    }
}
