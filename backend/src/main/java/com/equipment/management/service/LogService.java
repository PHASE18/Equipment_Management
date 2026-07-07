package com.equipment.management.service;

import com.equipment.management.common.result.PageResult;
import com.equipment.management.dto.request.LogQuery;
import com.equipment.management.entity.DeviceStatusLog;

import java.util.Map;

public interface LogService {

    PageResult<Map<String, Object>> loginLogs(LogQuery query);

    PageResult<Map<String, Object>> operationLogs(LogQuery query);

    PageResult<DeviceStatusLog> statusLogs(LogQuery query);
}
