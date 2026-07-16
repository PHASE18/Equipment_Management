package com.equipment.management.service.impl;

import com.equipment.management.common.constant.ErrorCode;
import com.equipment.management.common.exception.BusinessException;
import com.equipment.management.common.result.PageResult;
import com.equipment.management.dto.request.LogQuery;
import com.equipment.management.entity.DeviceStatusLog;
import com.equipment.management.entity.SysLoginLog;
import com.equipment.management.entity.SysOperationLog;
import com.equipment.management.service.DeviceStatusLogService;
import com.equipment.management.service.SysLoginLogService;
import com.equipment.management.service.SysOperationLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogAuditServiceTest {

    @Mock
    private SysLoginLogService sysLoginLogService;
    @Mock
    private SysOperationLogService sysOperationLogService;
    @Mock
    private DeviceStatusLogService deviceStatusLogService;

    private LogServiceImpl logService;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        logService = new LogServiceImpl(
                sysLoginLogService,
                sysOperationLogService,
                deviceStatusLogService,
                objectMapper
        );
    }

    @Test
    @DisplayName("登录日志查询应委托并转换为 Map")
    void loginLogs_shouldDelegateAndConvert() {
        SysLoginLog log = new SysLoginLog();
        log.setId(1L);
        log.setUsername("admin");
        log.setLoginIp("127.0.0.1");
        log.setResult(1);
        log.setLoginTime(LocalDateTime.of(2026, 7, 16, 12, 0));
        when(sysLoginLogService.pageQuery(any(LogQuery.class)))
                .thenReturn(PageResult.of(List.of(log), 1, 1, 20));

        PageResult<Map<String, Object>> result = logService.loginLogs(new LogQuery());

        assertEquals(1, result.getTotal());
        assertEquals("admin", result.getRecords().get(0).get("username"));
        verify(sysLoginLogService).pageQuery(any(LogQuery.class));
    }

    @Test
    @DisplayName("操作日志查询应委托并转换为 Map")
    void operationLogs_shouldDelegateAndConvert() {
        SysOperationLog log = new SysOperationLog();
        log.setId(2L);
        log.setOperationType("INSERT");
        log.setTableName("project");
        when(sysOperationLogService.pageQuery(any(LogQuery.class)))
                .thenReturn(PageResult.of(List.of(log), 1, 1, 20));

        PageResult<Map<String, Object>> result = logService.operationLogs(new LogQuery());

        assertEquals(1, result.getTotal());
        assertEquals("INSERT", result.getRecords().get(0).get("operationType"));
        assertEquals("project", result.getRecords().get(0).get("tableName"));
    }

    @Test
    @DisplayName("生命周期日志查询应直接返回实体分页")
    void statusLogs_shouldReturnEntityPage() {
        DeviceStatusLog log = new DeviceStatusLog();
        log.setId(3L);
        log.setDeviceId(10L);
        log.setNewStatusCode("IN_USE");
        when(deviceStatusLogService.pageQuery(any(LogQuery.class)))
                .thenReturn(PageResult.of(List.of(log), 1, 1, 20));

        PageResult<DeviceStatusLog> result = logService.statusLogs(new LogQuery());

        assertEquals(1, result.getTotal());
        assertEquals("IN_USE", result.getRecords().get(0).getNewStatusCode());
    }

    @Nested
    @DisplayName("日志禁止删除")
    class RemoveForbiddenTests {

        @Test
        void loginLog_removeForbidden() {
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> new SysLoginLogServiceImpl().removeLog(1L));
            assertEquals(ErrorCode.FORBIDDEN.getCode(), ex.getCode());
            assertTrue(ex.getMessage().contains("禁止删除"));
        }

        @Test
        void operationLog_removeForbidden() {
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> new SysOperationLogServiceImpl().removeLog(1L));
            assertEquals(ErrorCode.FORBIDDEN.getCode(), ex.getCode());
        }

        @Test
        void statusLog_removeForbidden() {
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> new DeviceStatusLogServiceImpl().removeLog(1L));
            assertEquals(ErrorCode.FORBIDDEN.getCode(), ex.getCode());
        }
    }
}
