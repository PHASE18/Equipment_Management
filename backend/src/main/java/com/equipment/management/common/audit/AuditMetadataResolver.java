package com.equipment.management.common.audit;

import com.equipment.management.common.context.UserContext;
import com.equipment.management.common.result.Result;
import com.equipment.management.common.util.RequestUtils;
import com.equipment.management.dto.request.DeviceStatusChangeRequest;
import com.equipment.management.dto.request.LoginRequest;
import com.equipment.management.dto.response.LoginResponse;
import com.equipment.management.entity.BaseEntity;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

@Component
public class AuditMetadataResolver {

    private static final Map<String, String> PATH_TABLE_MAP = Map.ofEntries(
            Map.entry("/api/device", "device"),
            Map.entry("/api/maintenance", "device_maintenance"),
            Map.entry("/api/attachment", "device_attachment"),
            Map.entry("/api/project", "project"),
            Map.entry("/api/user", "sys_user"),
            Map.entry("/api/department", "sys_department"),
            Map.entry("/api/role", "sys_role"),
            Map.entry("/api/permission/crud", "sys_permission"),
            Map.entry("/api/config", "sys_config"),
            Map.entry("/api/dict/crud", "sys_dict"),
            Map.entry("/api/device-config", "device_config"),
            Map.entry("/api/ip", "device_ip"),
            Map.entry("/api/role-permission", "sys_role_permission"),
            Map.entry("/api/user-role", "sys_user_role"),
            Map.entry("/api/device-project", "device_project"),
            Map.entry("/api/file", "device_attachment"),
            Map.entry("/api/excel", "device"),
            Map.entry("/api/device/status", "device"),
            Map.entry("/api/login", "sys_user"),
            Map.entry("/api/logout", "sys_user")
    );

    private final EntitySnapshotLoader entitySnapshotLoader;

    public AuditMetadataResolver(EntitySnapshotLoader entitySnapshotLoader) {
        this.entitySnapshotLoader = entitySnapshotLoader;
    }

    public AuditMetadata resolve(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        HttpServletRequest request = currentRequest();

        String requestPath = resolveRequestPath(method, joinPoint.getTarget().getClass());
        OperationType operationType = resolveOperationType(method, requestPath);
        Object requestBody = extractRequestBody(joinPoint.getArgs(), signature.getMethod());

        return AuditMetadata.builder()
                .operationType(operationType)
                .tableName(resolveTableName(requestPath, requestBody))
                .businessId(extractBusinessId(joinPoint.getArgs(), signature.getMethod(), requestBody, operationType))
                .operatorId(UserContext.getUserId())
                .requestPath(requestPath)
                .requestMethod(request != null ? request.getMethod() : null)
                .ip(request != null ? RequestUtils.resolveClientIp(request) : null)
                .browser(request != null ? RequestUtils.resolveBrowser(request) : null)
                .requestBody(requestBody)
                .build();
    }

    public void enrichAfterExecution(AuditMetadata metadata, Object result, boolean success, String errorMessage) {
        metadata.setSuccess(success);
        metadata.setErrorMessage(errorMessage);

        if (metadata.getOperationType() == OperationType.LOGIN && success && result instanceof Result<?> resultWrapper) {
            Object data = resultWrapper.getData();
            if (data instanceof LoginResponse loginResponse
                    && loginResponse.getUser() != null
                    && loginResponse.getUser().getId() != null) {
                metadata.setBusinessId(loginResponse.getUser().getId());
                metadata.setOperatorId(loginResponse.getUser().getId());
            }
        }
    }

    private HttpServletRequest currentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    private String resolveRequestPath(Method method, Class<?> controllerClass) {
        String classPath = extractMappingPath(controllerClass.getAnnotation(RequestMapping.class));
        String methodPath = extractMethodPath(method);
        return normalizePath(classPath, methodPath);
    }

    private String extractMethodPath(Method method) {
        if (method.isAnnotationPresent(PostMapping.class)) {
            return firstPath(method.getAnnotation(PostMapping.class).value(),
                    method.getAnnotation(PostMapping.class).path());
        }
        if (method.isAnnotationPresent(PutMapping.class)) {
            return firstPath(method.getAnnotation(PutMapping.class).value(),
                    method.getAnnotation(PutMapping.class).path());
        }
        if (method.isAnnotationPresent(DeleteMapping.class)) {
            return firstPath(method.getAnnotation(DeleteMapping.class).value(),
                    method.getAnnotation(DeleteMapping.class).path());
        }
        return "";
    }

    private String extractMappingPath(RequestMapping mapping) {
        if (mapping == null) {
            return "";
        }
        return firstPath(mapping.value(), mapping.path());
    }

    private String firstPath(String[] primary, String[] secondary) {
        if (primary != null && primary.length > 0 && StringUtils.hasText(primary[0])) {
            return primary[0];
        }
        if (secondary != null && secondary.length > 0 && StringUtils.hasText(secondary[0])) {
            return secondary[0];
        }
        return "";
    }

    private String normalizePath(String classPath, String methodPath) {
        String base = StringUtils.hasText(classPath) ? classPath : "";
        if (!StringUtils.hasText(methodPath)) {
            return base.isEmpty() ? "/" : base;
        }
        if (methodPath.startsWith("/api/")) {
            return methodPath;
        }
        if (base.endsWith("/") && methodPath.startsWith("/")) {
            return base + methodPath.substring(1);
        }
        if (!base.endsWith("/") && !methodPath.startsWith("/") && !base.isEmpty()) {
            return base + "/" + methodPath;
        }
        return base + methodPath;
    }

    private OperationType resolveOperationType(Method method, String requestPath) {
        if (requestPath.endsWith("/login")) {
            return OperationType.LOGIN;
        }
        if (requestPath.endsWith("/logout")) {
            return OperationType.LOGOUT;
        }
        if (requestPath.contains("/status/change") || requestPath.endsWith("/change")) {
            return OperationType.STATUS_CHANGE;
        }
        if (requestPath.endsWith("/import")) {
            return OperationType.IMPORT;
        }
        if (method.isAnnotationPresent(PostMapping.class)) {
            return OperationType.INSERT;
        }
        if (method.isAnnotationPresent(PutMapping.class)) {
            return OperationType.UPDATE;
        }
        if (method.isAnnotationPresent(DeleteMapping.class)) {
            return OperationType.DELETE;
        }
        return OperationType.UPDATE;
    }

    private String resolveTableName(String requestPath, Object requestBody) {
        for (Map.Entry<String, String> entry : PATH_TABLE_MAP.entrySet()) {
            if (requestPath.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        String tableName = entitySnapshotLoader.resolveTableName(requestBody);
        return tableName != null ? tableName : "unknown";
    }

    private Object extractRequestBody(Object[] args, Method method) {
        if (args == null || args.length == 0) {
            return null;
        }
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].isAnnotationPresent(RequestBody.class) && args[i] != null) {
                return args[i];
            }
        }
        for (Object arg : args) {
            if (arg == null || arg instanceof HttpServletRequest) {
                continue;
            }
            if (arg instanceof MultipartFile file) {
                return Map.of(
                        "fileName", file.getOriginalFilename(),
                        "fileSize", file.getSize(),
                        "contentType", file.getContentType()
                );
            }
            if (!(arg instanceof String) && !(arg instanceof Number) && !(arg instanceof Boolean)) {
                return arg;
            }
        }
        return null;
    }

    private Long extractBusinessId(Object[] args, Method method, Object requestBody, OperationType operationType) {
        if (requestBody instanceof BaseEntity baseEntity && baseEntity.getId() != null) {
            return baseEntity.getId();
        }
        if (requestBody instanceof DeviceStatusChangeRequest statusRequest) {
            return statusRequest.getDeviceId();
        }
        if (requestBody instanceof LoginRequest) {
            return null;
        }

        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].isAnnotationPresent(PathVariable.class) && args[i] instanceof Long id) {
                return id;
            }
        }
        if (operationType == OperationType.LOGOUT) {
            return UserContext.getUserId();
        }
        return null;
    }
}
