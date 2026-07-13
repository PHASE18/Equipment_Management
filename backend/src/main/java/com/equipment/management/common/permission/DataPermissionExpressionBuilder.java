package com.equipment.management.common.permission;

import com.equipment.management.common.context.UserContext;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.ParenthesedSelect;
import net.sf.jsqlparser.statement.select.PlainSelect;

/**
 * 数据权限 SQL 表达式构建器
 */
public final class DataPermissionExpressionBuilder {

    private DataPermissionExpressionBuilder() {
    }

    public static Expression denyAll() {
        return new EqualsTo(new LongValue(1), new LongValue(0));
    }

    public static Expression equalsColumn(Table table, String columnName, Long value) {
        return new EqualsTo(column(table, columnName), new LongValue(value));
    }

    public static Expression equalsColumn(Table table, String columnName, String value) {
        return new EqualsTo(column(table, columnName), new StringValue(value));
    }

    public static Expression deviceScopeSubquery(Table table, String deviceIdColumn, UserContext.LoginUser user) {
        Expression deviceFilter = buildDeviceFilter(user);
        if (deviceFilter == null) {
            return null;
        }
        return inSubquery(table, deviceIdColumn, "device", "id", deviceFilter);
    }

    public static Expression operatorScopeSubquery(Table table, String operatorColumn, UserContext.LoginUser user) {
        if (user.isDepartmentDataScope()) {
            if (user.getDepartmentId() == null) {
                return denyAll();
            }
            Expression userFilter = equalsColumn(new Table("sys_user"), "department_id", user.getDepartmentId());
            return inSubquery(table, operatorColumn, "sys_user", "id", userFilter);
        }
        if (user.getUserId() == null) {
            return denyAll();
        }
        return equalsColumn(table, operatorColumn, user.getUserId());
    }

    public static Expression loginLogScope(Table table, UserContext.LoginUser user) {
        if (user.isDepartmentDataScope()) {
            if (user.getDepartmentId() == null) {
                return denyAll();
            }
            Expression userFilter = equalsColumn(new Table("sys_user"), "department_id", user.getDepartmentId());
            return inSubquery(table, "username", "sys_user", "username", userFilter);
        }
        if (user.getUsername() == null) {
            return denyAll();
        }
        return equalsColumn(table, "username", user.getUsername());
    }

    public static Expression buildDirectScope(Table table, DataPermissionRule rule, UserContext.LoginUser user) {
        if (user.isDepartmentDataScope()) {
            if (rule.departmentColumn() == null || user.getDepartmentId() == null) {
                return denyAll();
            }
            return equalsColumn(table, rule.departmentColumn(), user.getDepartmentId());
        }

        if (rule.ownerColumn() != null && user.getUserId() != null) {
            return equalsColumn(table, rule.ownerColumn(), user.getUserId());
        }
        return denyAll();
    }

    private static Expression buildDeviceFilter(UserContext.LoginUser user) {
        Table deviceTable = new Table("device");
        if (user.isDepartmentDataScope()) {
            if (user.getDepartmentId() == null) {
                return denyAll();
            }
            return equalsColumn(deviceTable, "department_id", user.getDepartmentId());
        }
        if (user.getUserId() == null) {
            return denyAll();
        }
        return equalsColumn(deviceTable, "manager_user_id", user.getUserId());
    }

    private static Expression inSubquery(Table table, String columnName, String subTableName,
                                         String subColumnName, Expression subWhere) {
        PlainSelect select = new PlainSelect();
        select.addSelectItem(new Column(subTableName + "." + subColumnName));
        select.setFromItem(new Table(subTableName));
        select.setWhere(subWhere);

        ParenthesedSelect parenthesedSelect = new ParenthesedSelect();
        parenthesedSelect.setSelect(select);

        InExpression inExpression = new InExpression();
        inExpression.setLeftExpression(column(table, columnName));
        inExpression.setRightExpression(parenthesedSelect);
        return inExpression;
    }

    private static Column column(Table table, String columnName) {
        if (table.getAlias() != null) {
            return new Column(table.getAlias().getName() + "." + columnName);
        }
        return new Column(table.getName() + "." + columnName);
    }
}
