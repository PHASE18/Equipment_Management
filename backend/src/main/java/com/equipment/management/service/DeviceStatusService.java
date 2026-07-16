package com.equipment.management.service;

import com.equipment.management.common.query.PageQuery;
import com.equipment.management.common.result.PageResult;
import com.equipment.management.dto.request.DeviceStatusChangeRequest;
import com.equipment.management.dto.response.DeviceStatusChangeResponse;
import com.equipment.management.dto.response.DeviceStatusLogResponse;
import com.equipment.management.entity.DeviceStatusLog;

import java.util.List;

public interface DeviceStatusService {

    PageResult<DeviceStatusLog> listByDeviceId(Long deviceId, PageQuery query);

    List<DeviceStatusLogResponse> listHistory(Long deviceId);

    List<String> allowedNextStatuses(Long deviceId);

    DeviceStatusChangeResponse changeStatus(DeviceStatusChangeRequest request);
}
