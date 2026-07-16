package com.equipment.management.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class StatisticsFilter {

    private Long departmentId;
    private Long projectId;
    private String brandCode;
    private String deviceTypeCode;
    private LocalDate startDate;
    private LocalDate endDate;

    private boolean allScope = true;
    private Long scopeDepartmentId;
    private Long scopeUserId;
}
