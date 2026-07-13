package com.equipment.management.common.audit;

import lombok.Getter;

@Getter
public enum OperationType {
    INSERT("INSERT"),
    UPDATE("UPDATE"),
    DELETE("DELETE"),
    STATUS_CHANGE("STATUS_CHANGE"),
    LOGIN("LOGIN"),
    LOGOUT("LOGOUT"),
    IMPORT("IMPORT");

    private final String code;

    OperationType(String code) {
        this.code = code;
    }
}
