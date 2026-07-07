package com.equipment.management.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.equipment.management.annotation.RequireAuth;
import com.equipment.management.common.controller.BaseCrudController;
import com.equipment.management.common.query.PageQuery;
import com.equipment.management.common.result.PageResult;
import com.equipment.management.common.result.Result;
import com.equipment.management.dto.request.MaintenanceQuery;
import com.equipment.management.entity.DeviceMaintenance;
import com.equipment.management.service.DeviceMaintenanceService;
import jakarta.validation.Valid;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequireAuth
@RequestMapping("/api/maintenance")
public class DeviceMaintenanceController extends BaseCrudController<DeviceMaintenanceService, DeviceMaintenance> {

    public DeviceMaintenanceController(DeviceMaintenanceService deviceMaintenanceService) {
        super(deviceMaintenanceService);
    }

    @GetMapping("/list")
    public Result<PageResult<DeviceMaintenance>> list(@Valid MaintenanceQuery query) {
        return Result.success(baseService.page(query));
    }

    @Override
    protected QueryWrapper<DeviceMaintenance> buildQueryWrapper(PageQuery query) {
        QueryWrapper<DeviceMaintenance> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like("maintenance_person", query.getKeyword())
                    .or().like("fault_description", query.getKeyword()));
        }
        wrapper.orderByDesc("create_time");
        return wrapper;
    }
}
