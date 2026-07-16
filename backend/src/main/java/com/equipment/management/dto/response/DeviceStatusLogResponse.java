package com.equipment.management.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class DeviceStatusLogResponse {

    private Long id;
    private Long deviceId;
    private String oldStatusCode;
    private String oldStatusName;
    private String newStatusCode;
    private String newStatusName;
    private String changeReason;
    private String remark;
    private Long operatorId;
    private String operatorName;
    private LocalDateTime changeTime;
}
