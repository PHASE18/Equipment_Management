package com.equipment.management.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class MaintenanceDetailResponse {

    private Long id;
    private Long deviceId;
    private String deviceNo;
    private String deviceName;
    private LocalDate maintenanceDate;
    private String maintenancePerson;
    private String maintenanceCompany;
    private String faultTypeCode;
    private String faultTypeName;
    private String faultReason;
    private String faultDescription;
    private String replaceParts;
    private BigDecimal maintenanceCost;
    private LocalDate recoverDate;
    private Integer isResolved;
    private String attachmentPath;
    private String remark;
    private LocalDateTime createTime;
    private List<FileUploadResponse> attachments;
}
