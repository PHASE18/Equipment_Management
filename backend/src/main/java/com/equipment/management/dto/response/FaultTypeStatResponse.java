package com.equipment.management.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FaultTypeStatResponse {

    private String faultTypeCode;
    private String faultTypeName;
    private Long count;
}
