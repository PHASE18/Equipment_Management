package com.equipment.management.aspect;

import com.equipment.management.annotation.IgnoreAudit;
import com.equipment.management.common.audit.AuditJsonUtils;
import com.equipment.management.common.audit.AuditLogService;
import com.equipment.management.common.audit.AuditMetadata;
import com.equipment.management.common.audit.AuditMetadataResolver;
import com.equipment.management.common.audit.EntitySnapshotLoader;
import com.equipment.management.common.audit.OperationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
@Order(100)
@RequiredArgsConstructor
public class OperationLogAspect {

    private final AuditMetadataResolver auditMetadataResolver;
    private final EntitySnapshotLoader entitySnapshotLoader;
    private final AuditLogService auditLogService;
    private final AuditJsonUtils auditJsonUtils;

    @Pointcut("within(com.equipment.management.controller..*)")
    public void controllerLayer() {
    }

    @Pointcut("@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.DeleteMapping)")
    public void writeOperation() {
    }

    @Around("controllerLayer() && writeOperation()")
    public Object aroundWriteOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        if (shouldIgnore(joinPoint)) {
            return joinPoint.proceed();
        }

        AuditMetadata metadata = auditMetadataResolver.resolve(joinPoint);
        String beforeJson = loadBeforeSnapshot(metadata);
        Object result = null;
        boolean success = true;
        String errorMessage = null;

        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable ex) {
            success = false;
            errorMessage = ex.getMessage();
            throw ex;
        } finally {
            auditMetadataResolver.enrichAfterExecution(metadata, result, success, errorMessage);
            String afterJson = auditJsonUtils.buildResultJson(
                    metadata.getRequestBody(), metadata.isSuccess(), metadata.getErrorMessage());
            auditLogService.record(metadata, beforeJson, afterJson);
        }
    }

    private String loadBeforeSnapshot(AuditMetadata metadata) {
        OperationType operationType = metadata.getOperationType();
        if (operationType == OperationType.UPDATE
                || operationType == OperationType.DELETE
                || operationType == OperationType.STATUS_CHANGE) {
            return entitySnapshotLoader.loadBeforeJson(metadata.getTableName(), metadata.getBusinessId());
        }
        return null;
    }

    private boolean shouldIgnore(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Class<?> targetClass = joinPoint.getTarget().getClass();
        return method.isAnnotationPresent(IgnoreAudit.class) || targetClass.isAnnotationPresent(IgnoreAudit.class);
    }
}
