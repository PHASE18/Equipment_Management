package com.equipment.management.controller;

import com.equipment.management.annotation.RequireAuth;
import com.equipment.management.common.query.PageQuery;
import com.equipment.management.common.result.PageResult;
import com.equipment.management.common.result.Result;
import com.equipment.management.dto.request.DeviceStatusChangeRequest;
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

@RestController
@RequireAuth
@RequestMapping("/api/device/status")
@RequiredArgsConstructor
public class DeviceStatusController {

    private final DeviceStatusService deviceStatusService;

    @GetMapping("/list/{deviceId}")
    public Result<PageResult<DeviceStatusLog>> list(@PathVariable Long deviceId, @Valid PageQuery query) {
        return Result.success(deviceStatusService.listByDeviceId(deviceId, query));
    }

    @PostMapping("/change")
    public Result<Void> changeStatus(@Valid @RequestBody DeviceStatusChangeRequest request) {
        deviceStatusService.changeStatus(request);
        return Result.success();
    }
}
