package com.equipment.management.service;

import com.equipment.management.common.result.PageResult;
import com.equipment.management.common.service.BaseCrudService;
import com.equipment.management.dto.request.MaintenanceQuery;
import com.equipment.management.entity.DeviceMaintenance;

public interface DeviceMaintenanceService extends BaseCrudService<DeviceMaintenance> {

    PageResult<DeviceMaintenance> page(MaintenanceQuery query);
}
