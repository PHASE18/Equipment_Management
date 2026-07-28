package com.equipment.management.common.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * 设备生命周期主状态及合法流转（方案 A：维修用 maintaining_flag，与主状态并存）。
 * <p>
 * MAINTAINING / STANDBY 仅保留用于历史日志展示，不再作为可流转主状态。
 */
@Getter
public enum DeviceLifecycleStatus {

    PURCHASING("PURCHASING", "采购中", Set.of("IN_STOCK")),
    IN_STOCK("IN_STOCK", "库存", Set.of("PENDING_ONLINE")),
    PENDING_ONLINE("PENDING_ONLINE", "待上架", Set.of("IN_USE")),
    IN_USE("IN_USE", "在用", Set.of("STOPPED", "OFFLINE")),
    STOPPED("STOPPED", "停用（机柜）", Set.of("IN_USE", "OFFLINE")),
    OFFLINE("OFFLINE", "下架（库房）", Set.of("IN_USE", "SCRAPPED")),
    SCRAPPED("SCRAPPED", "报废", Collections.emptySet()),

    /** @deprecated 方案 A 后仅历史日志兼容 */
    MAINTAINING("MAINTAINING", "维修中", Collections.emptySet()),
    /** @deprecated 已合并为 STOPPED */
    STANDBY("STANDBY", "备用", Collections.emptySet());

    private final String code;
    private final String label;
    private final Set<String> allowedNextCodes;

    DeviceLifecycleStatus(String code, String label, Set<String> allowedNextCodes) {
        this.code = code;
        this.label = label;
        this.allowedNextCodes = allowedNextCodes;
    }

    public static DeviceLifecycleStatus fromCode(String code) {
        return Arrays.stream(values())
                .filter(item -> item.code.equals(code))
                .findFirst()
                .orElse(null);
    }

    public static boolean isValidTransition(String fromCode, String toCode) {
        if (fromCode == null || toCode == null || fromCode.equals(toCode)) {
            return false;
        }
        DeviceLifecycleStatus from = fromCode(fromCode);
        DeviceLifecycleStatus to = fromCode(toCode);
        if (from == null || to == null) {
            return false;
        }
        return from.allowedNextCodes.contains(to.code);
    }

    public static List<String> allowedNextStatuses(String currentCode) {
        DeviceLifecycleStatus current = fromCode(currentCode);
        if (current == null) {
            return Collections.emptyList();
        }
        return current.allowedNextCodes.stream().sorted().toList();
    }

    public static String labelOf(String code) {
        DeviceLifecycleStatus status = fromCode(code);
        return status != null ? status.label : code;
    }

    /** 主状态 + 维修标志的展示名（维修中/在用、维修中/停用）。 */
    public static String displayLabel(String statusCode, Integer maintainingFlag) {
        boolean maintaining = maintainingFlag != null && maintainingFlag == 1;
        if (!maintaining) {
            return labelOf(statusCode);
        }
        if ("IN_USE".equals(statusCode)) {
            return "维修中/在用";
        }
        if ("STOPPED".equals(statusCode)) {
            return "维修中/停用";
        }
        return labelOf(statusCode) + "（维修中）";
    }

    /** 在用、停用期间允许开维修工单。 */
    public static boolean canEnterMaintenance(String statusCode) {
        return "IN_USE".equals(statusCode) || "STOPPED".equals(statusCode);
    }

    /** 下架、报废时清除维修标志。 */
    public static boolean shouldClearMaintainingFlag(String statusCode) {
        return "OFFLINE".equals(statusCode) || "SCRAPPED".equals(statusCode);
    }
}
