package com.equipment.management.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.equipment.management.annotation.CrudPermission;
import com.equipment.management.annotation.RequireAuth;
import com.equipment.management.common.controller.BaseCrudController;
import com.equipment.management.common.query.PageQuery;
import com.equipment.management.common.result.PageResult;
import com.equipment.management.common.result.Result;
import com.equipment.management.dto.request.MaintenanceQuery;
import com.equipment.management.dto.response.FaultTypeStatResponse;
import com.equipment.management.dto.response.MaintenanceDetailResponse;
import com.equipment.management.entity.DeviceMaintenance;
import com.equipment.management.service.DeviceMaintenanceService;
import jakarta.validation.Valid;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequireAuth
@CrudPermission(module = "maintenance")
@RequestMapping("/api/maintenance")
public class DeviceMaintenanceController extends BaseCrudController<DeviceMaintenanceService, DeviceMaintenance> {

    public DeviceMaintenanceController(DeviceMaintenanceService deviceMaintenanceService) {
        super(deviceMaintenanceService);
    }

    @GetMapping("/list")
    public Result<PageResult<MaintenanceDetailResponse>> list(@Valid MaintenanceQuery query) {
        return Result.success(baseService.page(query));
    }

    @GetMapping("/fault-stats")
    public Result<List<FaultTypeStatResponse>> faultStats() {
        return Result.success(baseService.faultTypeStatistics());
    }

    @GetMapping("/detail/{id}")
    public Result<MaintenanceDetailResponse> detail(@PathVariable Long id) {
        return Result.success(baseService.getDetailVo(id));
    }

    @Override
    @PostMapping
    public Result<Void> create(@Valid @RequestBody DeviceMaintenance entity) {
        baseService.createMaintenance(entity);
        return Result.success();
    }

    @PostMapping("/submit")
    public Result<Long> submit(@Valid @RequestBody DeviceMaintenance entity) {
        baseService.createMaintenance(entity);
        return Result.success(entity.getId());
    }

    @Override
    @PutMapping
    public Result<Void> update(@Valid @RequestBody DeviceMaintenance entity) {
        baseService.updateMaintenance(entity);
        return Result.success();
    }

    @PutMapping("/{id}/complete")
    public Result<Void> complete(@PathVariable Long id) {
        baseService.completeMaintenance(id);
        return Result.success();
    }

    @Override
    protected QueryWrapper<DeviceMaintenance> buildQueryWrapper(PageQuery query) {
        QueryWrapper<DeviceMaintenance> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like("maintenance_person", query.getKeyword())
                    .or().like("fault_description", query.getKeyword()));
        }
        wrapper.orderByDesc("maintenance_date");
        return wrapper;
    }
}
