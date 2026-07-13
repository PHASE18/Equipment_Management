package com.equipment.management.common.audit;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuditMetadata {

    private OperationType operationType;
    private String tableName;
    private Long businessId;
    private Long operatorId;
    private String requestPath;
    private String requestMethod;
    private String ip;
    private String browser;
    private Object requestBody;
    private boolean success;
    private String errorMessage;
}
