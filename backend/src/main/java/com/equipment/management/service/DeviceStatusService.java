package com.equipment.management.service;

import com.equipment.management.common.result.PageResult;
import com.equipment.management.common.query.PageQuery;
import com.equipment.management.dto.request.DeviceStatusChangeRequest;
import com.equipment.management.entity.DeviceStatusLog;

public interface DeviceStatusService {

    PageResult<DeviceStatusLog> listByDeviceId(Long deviceId, PageQuery query);

    void changeStatus(DeviceStatusChangeRequest request);
}
