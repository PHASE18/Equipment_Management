package com.equipment.management.controller;

import com.equipment.management.annotation.RequireAuth;
import com.equipment.management.common.result.PageResult;
import com.equipment.management.common.result.Result;
import com.equipment.management.dto.request.LogQuery;
import com.equipment.management.entity.DeviceStatusLog;
import com.equipment.management.service.LogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequireAuth
@RequestMapping("/api/log")
@RequiredArgsConstructor
public class LogController {

    private final LogService logService;

    @GetMapping("/login")
    public Result<PageResult<Map<String, Object>>> loginLogs(@Valid LogQuery query) {
        return Result.success(logService.loginLogs(query));
    }

    @GetMapping("/operation")
    public Result<PageResult<Map<String, Object>>> operationLogs(@Valid LogQuery query) {
        return Result.success(logService.operationLogs(query));
    }

    @GetMapping("/status")
    public Result<PageResult<DeviceStatusLog>> statusLogs(@Valid LogQuery query) {
        return Result.success(logService.statusLogs(query));
    }
}
