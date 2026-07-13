package com.equipment.management.common.audit;

public interface AuditLogService {

    void record(AuditMetadata metadata, String beforeJson, String afterJson);
}
