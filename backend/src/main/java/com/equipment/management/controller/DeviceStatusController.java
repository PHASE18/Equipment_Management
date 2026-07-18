package com.equipment.management.controller;

import com.equipment.management.annotation.RequireAuth;
import com.equipment.management.annotation.RequirePermission;
import com.equipment.management.common.query.PageQuery;
import com.equipment.management.common.result.PageResult;
import com.equipment.management.common.result.Result;
import com.equipment.management.dto.request.DeviceStatusChangeRequest;
import com.equipment.management.dto.response.DeviceStatusChangeResponse;
import com.equipment.management.dto.response.DeviceStatusLogResponse;
import com.equipment.management.entity.DeviceStatusLog;
import com.equipment.management.service.DeviceStatusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequireAuth
@RequestMapping("/api/device/status")
@RequiredArgsConstructor
/** 设备生命周期状态迁移接口。 */
public class DeviceStatusController {

    private final DeviceStatusService deviceStatusService;

    @RequirePermission("device:view")
    @GetMapping("/list/{deviceId}")
    public Result<PageResult<DeviceStatusLog>> list(@PathVariable Long deviceId, @Valid PageQuery query) {
        return Result.success(deviceStatusService.listByDeviceId(deviceId, query));
    }

    @RequirePermission("device:view")
    @GetMapping("/history/{deviceId}")
    public Result<List<DeviceStatusLogResponse>> history(@PathVariable Long deviceId) {
        return Result.success(deviceStatusService.listHistory(deviceId));
    }

    @RequirePermission("device:view")
    @GetMapping("/transitions/{deviceId}")
    public Result<List<String>> transitions(@PathVariable Long deviceId) {
        return Result.success(deviceStatusService.allowedNextStatuses(deviceId));
    }

    @RequirePermission("device:edit")
    @PostMapping("/change")
    public Result<DeviceStatusChangeResponse> changeStatus(@Valid @RequestBody DeviceStatusChangeRequest request) {
        return Result.success(deviceStatusService.changeStatus(request));
    }
}
