package com.equipment.management.controller;

import com.equipment.management.annotation.RequireAuth;
import com.equipment.management.common.result.PageResult;
import com.equipment.management.common.result.Result;
import com.equipment.management.dto.request.LogQuery;
import com.equipment.management.entity.DeviceStatusLog;
import com.equipment.management.service.DeviceStatusLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequireAuth
@RequestMapping("/api/device-status-log")
@RequiredArgsConstructor
public class DeviceStatusLogController {

    private final DeviceStatusLogService deviceStatusLogService;

    @GetMapping("/page")
    public Result<PageResult<DeviceStatusLog>> page(@Valid LogQuery query) {
        return Result.success(deviceStatusLogService.pageQuery(query));
    }

    @GetMapping("/{id}")
    public Result<DeviceStatusLog> getById(@PathVariable Long id) {
        return Result.success(deviceStatusLogService.getDetail(id));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        deviceStatusLogService.removeLog(id);
        return Result.success();
    }
}
