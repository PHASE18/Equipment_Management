package com.equipment.management.service;

import com.equipment.management.common.result.PageResult;
import com.equipment.management.dto.request.LogQuery;
import com.equipment.management.entity.DeviceStatusLog;

import java.util.Map;

/** 登录日志和操作日志查询领域服务。 */
public interface LogService {

    PageResult<Map<String, Object>> loginLogs(LogQuery query);

    PageResult<Map<String, Object>> operationLogs(LogQuery query);

    PageResult<DeviceStatusLog> statusLogs(LogQuery query);
}
