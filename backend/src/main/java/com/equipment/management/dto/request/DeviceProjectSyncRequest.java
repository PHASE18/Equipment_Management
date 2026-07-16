package com.equipment.management.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class DeviceProjectSyncRequest {

    @NotNull(message = "设备ID不能为空")
    private Long deviceId;

    private List<Long> projectIds;
}
