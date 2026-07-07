package com.equipment.management.dto.request;

import com.equipment.management.common.query.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MaintenanceQuery extends PageQuery {

    private Long deviceId;
    private String faultType;
}
