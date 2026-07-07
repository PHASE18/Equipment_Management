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

    @NotBlank(message = "新状态不能为空")
    private String newStatus;

    private String reason;
}
