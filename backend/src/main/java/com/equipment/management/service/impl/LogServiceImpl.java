package com.equipment.management.service.impl;

import com.equipment.management.common.result.PageResult;
import com.equipment.management.dto.request.LogQuery;
import com.equipment.management.entity.DeviceStatusLog;
import com.equipment.management.service.LogService;
import org.springframework.stereotype.Service;

@Service
public class LogServiceImpl implements LogService {

    @Override
    public PageResult<java.util.Map<String, Object>> loginLogs(LogQuery query) {
        // TODO: 分页查询 sys_login_log + 数据权限
        return PageResult.empty(query.getPageNum(), query.getPageSize());
    }

    @Override
    public PageResult<java.util.Map<String, Object>> operationLogs(LogQuery query) {
        // TODO: 分页查询 sys_operation_log + 数据权限
        return PageResult.empty(query.getPageNum(), query.getPageSize());
    }

    @Override
    public PageResult<DeviceStatusLog> statusLogs(LogQuery query) {
        // TODO: 分页查询 device_status_log
        return PageResult.empty(query.getPageNum(), query.getPageSize());
    }
}
