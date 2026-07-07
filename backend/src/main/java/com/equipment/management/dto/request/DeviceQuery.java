package com.equipment.management.dto.request;

import com.equipment.management.common.query.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DeviceQuery extends PageQuery {

    private String deviceNo;
    private String deviceName;
    private String brand;
    private String model;
    private String status;
    private Long departmentId;
    private Long projectId;
}
