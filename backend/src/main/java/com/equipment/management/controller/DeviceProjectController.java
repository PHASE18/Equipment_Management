package com.equipment.management.controller;

import com.equipment.management.annotation.RequireAuth;
import com.equipment.management.common.query.PageQuery;
import com.equipment.management.common.result.PageResult;
import com.equipment.management.common.result.Result;
import com.equipment.management.dto.request.DeviceProjectSyncRequest;
import com.equipment.management.dto.request.ProjectBindRequest;
import com.equipment.management.entity.DeviceProject;
import com.equipment.management.service.DeviceProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequireAuth
@RequestMapping("/api/device-project")
@RequiredArgsConstructor
/** 设备与项目关联关系接口。 */
public class DeviceProjectController {

    private final DeviceProjectService deviceProjectService;

    @GetMapping("/page")
    public Result<PageResult<DeviceProject>> page(@Valid PageQuery query,
                                                  @RequestParam(required = false) Long deviceId,
                                                  @RequestParam(required = false) Long projectId) {
        return Result.success(deviceProjectService.pageQuery(query, deviceId, projectId));
    }

    @GetMapping("/by-device/{deviceId}")
    public Result<List<Long>> listProjectIdsByDevice(@PathVariable Long deviceId) {
        return Result.success(deviceProjectService.listProjectIdsByDeviceId(deviceId));
    }

    @PostMapping("/sync")
    public Result<Void> sync(@Valid @RequestBody DeviceProjectSyncRequest request) {
        deviceProjectService.syncProjects(request);
        return Result.success();
    }

    @PostMapping("/bind")
    public Result<Void> bind(@Valid @RequestBody ProjectBindRequest request) {
        deviceProjectService.bind(request);
        return Result.success();
    }

    @DeleteMapping("/unbind")
    public Result<Void> unbind(@Valid @RequestBody ProjectBindRequest request) {
        deviceProjectService.unbind(request);
        return Result.success();
    }
}
