package com.equipment.management.service.impl;

import com.equipment.management.common.result.PageResult;
import com.equipment.management.dto.request.LogQuery;
import com.equipment.management.entity.DeviceStatusLog;
import com.equipment.management.entity.SysLoginLog;
import com.equipment.management.entity.SysOperationLog;
import com.equipment.management.service.DeviceStatusLogService;
import com.equipment.management.service.LogService;
import com.equipment.management.service.SysLoginLogService;
import com.equipment.management.service.SysOperationLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
/** 日志查询服务实现。 */
public class LogServiceImpl implements LogService {

    private final SysLoginLogService sysLoginLogService;
    private final SysOperationLogService sysOperationLogService;
    private final DeviceStatusLogService deviceStatusLogService;
    private final ObjectMapper objectMapper;

    @Override
    public PageResult<Map<String, Object>> loginLogs(LogQuery query) {
        return toMapPage(sysLoginLogService.pageQuery(query));
    }

    @Override
    public PageResult<Map<String, Object>> operationLogs(LogQuery query) {
        return toMapPage(sysOperationLogService.pageQuery(query));
    }

    @Override
    public PageResult<DeviceStatusLog> statusLogs(LogQuery query) {
        return deviceStatusLogService.pageQuery(query);
    }

    private <T> PageResult<Map<String, Object>> toMapPage(PageResult<T> page) {
        List<Map<String, Object>> records = page.getRecords().stream().map(this::toMap).toList();
        return PageResult.of(records, page.getTotal(), page.getPageNum(), page.getPageSize());
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> toMap(Object entity) {
        return objectMapper.convertValue(entity, Map.class);
    }
}
