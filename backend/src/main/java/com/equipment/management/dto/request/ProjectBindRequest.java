package com.equipment.management.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProjectBindRequest {

    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    @NotNull(message = "设备ID不能为空")
    private Long deviceId;
}
