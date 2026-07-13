package com.equipment.management.common.permission;

/**
 * 数据权限表规则
 */
public record DataPermissionRule(DataPermissionScope scope, String departmentColumn, String ownerColumn,
                                 String deviceIdColumn, String operatorColumn) {

    public static DataPermissionRule direct(String departmentColumn, String ownerColumn) {
        return new DataPermissionRule(DataPermissionScope.DIRECT, departmentColumn, ownerColumn, null, null);
    }

    public static DataPermissionRule viaDevice(String deviceIdColumn) {
        return new DataPermissionRule(DataPermissionScope.VIA_DEVICE, null, null, deviceIdColumn, null);
    }

    public static DataPermissionRule viaOperator(String operatorColumn) {
        return new DataPermissionRule(DataPermissionScope.VIA_OPERATOR, null, null, null, operatorColumn);
    }

    public static DataPermissionRule loginLog() {
        return new DataPermissionRule(DataPermissionScope.LOGIN_LOG, null, null, null, null);
    }
}
