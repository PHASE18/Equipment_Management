package com.equipment.management.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DeviceStatusChangeResponse {

    private Long deviceId;
    private String oldStatusCode;
    private String oldStatusName;
    private String newStatusCode;
    private String newStatusName;
    private List<String> allowedNextStatuses;
    private List<DeviceStatusLogResponse> history;
}
