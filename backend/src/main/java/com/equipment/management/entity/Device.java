package com.equipment.management.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("device")
public class Device extends BaseEntity {

    private String deviceNo;
    private String deviceName;
    private String sn;
    private String assetNo;
    private String brandCode;
    private String model;
    private String deviceTypeCode;
    private Long departmentId;
    private Long managerUserId;
    private String supplier;
    private String maintenanceCompany;
    private LocalDate purchaseDate;
    private LocalDate warrantyEnd;
    private String statusCode;
    private String cabinet;
    private String location;
    private String remark;
}
