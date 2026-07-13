package com.equipment.management.common.permission;

import com.baomidou.mybatisplus.extension.plugins.handler.MultiDataPermissionHandler;
import com.equipment.management.annotation.IgnoreDataPermission;
import com.equipment.management.common.context.UserContext;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Table;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;

@Component
public class DataPermissionHandler implements MultiDataPermissionHandler {

    private static final Map<String, DataPermissionRule> TABLE_RULES = Map.ofEntries(
            Map.entry("device", DataPermissionRule.direct("department_id", "manager_user_id")),
            Map.entry("project", DataPermissionRule.direct("department_id", null)),
            Map.entry("sys_user", DataPermissionRule.direct("department_id", "id")),
            Map.entry("device_maintenance", DataPermissionRule.viaDevice("device_id")),
            Map.entry("device_attachment", DataPermissionRule.viaDevice("device_id")),
            Map.entry("device_config", DataPermissionRule.viaDevice("device_id")),
            Map.entry("device_ip", DataPermissionRule.viaDevice("device_id")),
            Map.entry("device_project", DataPermissionRule.viaDevice("device_id")),
            Map.entry("device_status_log", DataPermissionRule.viaDevice("device_id")),
            Map.entry("sys_operation_log", DataPermissionRule.viaOperator("operator_id")),
            Map.entry("sys_login_log", DataPermissionRule.loginLog())
    );

    @Override
    public Expression getSqlSegment(Table table, Expression where, String mappedStatementId) {
        if (shouldIgnore(mappedStatementId)) {
            return null;
        }

        UserContext.LoginUser user = UserContext.get();
        if (user == null || user.isAllDataScope()) {
            return null;
        }

        DataPermissionRule rule = TABLE_RULES.get(normalizeTableName(table.getName()));
        if (rule == null) {
            return null;
        }

        return buildScopeExpression(table, rule, user);
    }

    private Expression buildScopeExpression(Table table, DataPermissionRule rule, UserContext.LoginUser user) {
        return switch (rule.scope()) {
            case DIRECT -> DataPermissionExpressionBuilder.buildDirectScope(table, rule, user);
            case VIA_DEVICE -> DataPermissionExpressionBuilder.deviceScopeSubquery(table, rule.deviceIdColumn(), user);
            case VIA_OPERATOR -> DataPermissionExpressionBuilder.operatorScopeSubquery(table, rule.operatorColumn(), user);
            case LOGIN_LOG -> DataPermissionExpressionBuilder.loginLogScope(table, user);
        };
    }

    private String normalizeTableName(String tableName) {
        if (tableName == null) {
            return "";
        }
        return tableName.replace("`", "").toLowerCase();
    }

    private boolean shouldIgnore(String mappedStatementId) {
        int methodSeparator = mappedStatementId.lastIndexOf('.');
        if (methodSeparator < 0) {
            return false;
        }
        String className = mappedStatementId.substring(0, methodSeparator);
        String methodName = mappedStatementId.substring(methodSeparator + 1);
        try {
            Class<?> mapperClass = Class.forName(className);
            if (mapperClass.isAnnotationPresent(IgnoreDataPermission.class)) {
                return true;
            }
            for (Method method : mapperClass.getMethods()) {
                if (method.getName().equals(methodName)
                        && method.isAnnotationPresent(IgnoreDataPermission.class)) {
                    return true;
                }
            }
            return false;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
