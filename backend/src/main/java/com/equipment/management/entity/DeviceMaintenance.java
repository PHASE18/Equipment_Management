package com.equipment.management.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("device_maintenance")
/** 设备维修工单，记录故障、处理过程、费用和维修状态。 */
public class DeviceMaintenance extends BaseEntity {

    private Long deviceId;
    private LocalDate maintenanceDate;
    private String maintenancePerson;
    private String maintenanceCompany;
    private String faultTypeCode;
    private String faultReason;
    private String faultDescription;
    private String replaceParts;
    private BigDecimal maintenanceCost;
    private LocalDate recoverDate;
    private Integer isResolved;
    private String attachmentPath;
    private String remark;
}
