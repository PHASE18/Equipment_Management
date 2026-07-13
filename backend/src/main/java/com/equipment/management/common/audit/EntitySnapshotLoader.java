package com.equipment.management.common.audit;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.equipment.management.entity.BaseEntity;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class EntitySnapshotLoader {

    private static final String MAPPER_PACKAGE = "com.equipment.management.mapper.";

    private static final Map<String, Class<?>> TABLE_ENTITY_MAP = Map.ofEntries(
            Map.entry("device", com.equipment.management.entity.Device.class),
            Map.entry("device_maintenance", com.equipment.management.entity.DeviceMaintenance.class),
            Map.entry("device_attachment", com.equipment.management.entity.DeviceAttachment.class),
            Map.entry("device_config", com.equipment.management.entity.DeviceConfig.class),
            Map.entry("device_ip", com.equipment.management.entity.DeviceIp.class),
            Map.entry("device_project", com.equipment.management.entity.DeviceProject.class),
            Map.entry("project", com.equipment.management.entity.Project.class),
            Map.entry("sys_user", com.equipment.management.entity.SysUser.class),
            Map.entry("sys_department", com.equipment.management.entity.SysDepartment.class),
            Map.entry("sys_role", com.equipment.management.entity.SysRole.class),
            Map.entry("sys_permission", com.equipment.management.entity.SysPermission.class),
            Map.entry("sys_dict", com.equipment.management.entity.SysDict.class),
            Map.entry("sys_config", com.equipment.management.entity.SysConfig.class)
    );

    private final SqlSessionFactory sqlSessionFactory;
    private final AuditJsonUtils auditJsonUtils;
    private final Map<Class<?>, Class<?>> mapperClassCache = new ConcurrentHashMap<>();

    public String loadBeforeJson(String tableName, Long businessId) {
        return findEntity(tableName, businessId)
                .map(auditJsonUtils::toAuditJson)
                .orElse(null);
    }

    public Optional<Object> findEntity(String tableName, Long businessId) {
        if (tableName == null || businessId == null) {
            return Optional.empty();
        }
        Class<?> entityClass = TABLE_ENTITY_MAP.get(tableName);
        if (entityClass == null) {
            return Optional.empty();
        }
        return findEntity(entityClass, businessId);
    }

    @SuppressWarnings("unchecked")
    public Optional<Object> findEntity(Class<?> entityClass, Long businessId) {
        if (entityClass == null || businessId == null) {
            return Optional.empty();
        }
        try {
            Class<?> mapperClass = resolveMapperClass(entityClass);
            try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
                BaseMapper<Object> mapper = (BaseMapper<Object>) sqlSession.getMapper(mapperClass);
                return Optional.ofNullable(mapper.selectById(businessId));
            }
        } catch (ClassNotFoundException ex) {
            return Optional.empty();
        }
    }

    public String resolveTableName(Class<?> entityClass) {
        if (entityClass == null) {
            return null;
        }
        TableName tableName = entityClass.getAnnotation(TableName.class);
        if (tableName != null && !tableName.value().isBlank()) {
            return tableName.value();
        }
        return entityClass.getSimpleName();
    }

    public String resolveTableName(Object body) {
        if (body == null) {
            return null;
        }
        if (body instanceof BaseEntity) {
            return resolveTableName(body.getClass());
        }
        return null;
    }

    private Class<?> resolveMapperClass(Class<?> entityClass) throws ClassNotFoundException {
        return mapperClassCache.computeIfAbsent(entityClass, clazz -> {
            try {
                return Class.forName(MAPPER_PACKAGE + clazz.getSimpleName() + "Mapper");
            } catch (ClassNotFoundException ex) {
                throw new IllegalStateException("Mapper not found for entity: " + clazz.getSimpleName(), ex);
            }
        });
    }
}
