package com.equipment.management.common.constant;

import lombok.Getter;

/**
 * 业务错误码（与接口设计文档一致）
 */
@Getter
public enum ErrorCode {

    SUCCESS(200, "success"),
    BAD_REQUEST(400, "参数错误"),
    UNAUTHORIZED(401, "未登录"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "不存在"),
    CONFLICT(409, "数据冲突"),
    SERVER_ERROR(500, "服务器异常"),

    TOKEN_EXPIRED(1001, "Token失效"),
    LOGIN_FAILED(1002, "用户名密码错误"),
    PERMISSION_DENIED(1003, "权限不足"),
    DEVICE_NOT_FOUND(1004, "设备不存在"),
    DEVICE_NO_DUPLICATE(1005, "设备编号重复"),
    SN_DUPLICATE(1006, "SN重复"),
    BUSINESS_IP_DUPLICATE(1007, "业务IP重复"),
    MANAGEMENT_IP_DUPLICATE(1008, "管理IP重复"),
    PROJECT_NOT_FOUND(1009, "项目不存在"),
    MAINTENANCE_NOT_FOUND(1010, "维修记录不存在"),
    ATTACHMENT_NOT_FOUND(1011, "附件不存在"),
    EXCEL_FORMAT_ERROR(1012, "Excel格式错误"),
    EXCEL_IMPORT_FAILED(1013, "Excel导入失败"),
    STATUS_TRANSITION_INVALID(1014, "生命周期流转非法"),
    DATA_DELETED(1015, "数据已删除"),
    INTERNAL_ERROR(1099, "服务器异常");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
