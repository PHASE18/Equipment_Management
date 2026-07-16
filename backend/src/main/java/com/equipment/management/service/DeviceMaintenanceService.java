package com.equipment.management.service;

import com.equipment.management.common.result.PageResult;
import com.equipment.management.common.service.BaseCrudService;
import com.equipment.management.dto.request.MaintenanceQuery;
import com.equipment.management.dto.response.FaultTypeStatResponse;
import com.equipment.management.dto.response.MaintenanceDetailResponse;
import com.equipment.management.entity.DeviceMaintenance;

import java.util.List;

public interface DeviceMaintenanceService extends BaseCrudService<DeviceMaintenance> {

    PageResult<MaintenanceDetailResponse> page(MaintenanceQuery query);

    MaintenanceDetailResponse getDetailVo(Long id);

    void createMaintenance(DeviceMaintenance entity);

    void updateMaintenance(DeviceMaintenance entity);

    void completeMaintenance(Long id);

    List<FaultTypeStatResponse> faultTypeStatistics();
}
