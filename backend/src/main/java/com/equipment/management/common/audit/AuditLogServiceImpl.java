package com.equipment.management.common.audit;

import com.equipment.management.entity.SysOperationLog;
import com.equipment.management.mapper.SysOperationLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final SysOperationLogMapper sysOperationLogMapper;

    @Async
    @Override
    public void record(AuditMetadata metadata, String beforeJson, String afterJson) {
        try {
            SysOperationLog operationLog = new SysOperationLog();
            operationLog.setOperatorId(metadata.getOperatorId());
            operationLog.setOperationType(metadata.getOperationType().getCode());
            operationLog.setTableName(metadata.getTableName());
            operationLog.setBusinessId(metadata.getBusinessId());
            operationLog.setBeforeJson(beforeJson);
            operationLog.setAfterJson(afterJson);
            operationLog.setIp(metadata.getIp());
            operationLog.setBrowser(metadata.getBrowser());
            operationLog.setCreateTime(LocalDateTime.now());
            sysOperationLogMapper.insert(operationLog);
        } catch (Exception ex) {
            log.error("写入操作审计日志失败: path={}, type={}",
                    metadata.getRequestPath(), metadata.getOperationType(), ex);
        }
    }
}
