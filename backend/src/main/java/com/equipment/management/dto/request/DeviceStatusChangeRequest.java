package com.equipment.management.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 设备状态变更请求
 */
@Data
public class DeviceStatusChangeRequest {

    @NotNull(message = "设备ID不能为空")
    private Long deviceId;

    /**
     * 客户端感知的当前状态，用于并发校验（可选）
     */
    private String fromStatus;

    @NotBlank(message = "目标状态不能为空")
    private String toStatus;

    /**
     * 兼容旧字段
     */
    private String newStatus;

    @NotBlank(message = "变更原因不能为空")
    private String reason;

    private String remark;

    public String resolveTargetStatus() {
        if (toStatus != null && !toStatus.isBlank()) {
            return toStatus.trim();
        }
        return newStatus != null ? newStatus.trim() : null;
    }
}
