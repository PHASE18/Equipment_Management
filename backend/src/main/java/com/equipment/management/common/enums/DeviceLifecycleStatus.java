package com.equipment.management.common.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * 设备生命周期状态及合法流转规则（对应需求 4.4）
 */
@Getter
public enum DeviceLifecycleStatus {

    PURCHASING("PURCHASING", "采购中", Set.of("IN_STOCK")),
    IN_STOCK("IN_STOCK", "库存", Set.of("PENDING_ONLINE")),
    PENDING_ONLINE("PENDING_ONLINE", "待上架", Set.of("IN_USE")),
    IN_USE("IN_USE", "在用", Set.of("MAINTAINING", "STANDBY", "STOPPED")),
    MAINTAINING("MAINTAINING", "维修中", Set.of("IN_USE")),
    STANDBY("STANDBY", "备用", Set.of("IN_USE")),
    STOPPED("STOPPED", "停用", Set.of("SCRAPPED")),
    SCRAPPED("SCRAPPED", "报废", Collections.emptySet());

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
}
