package com.equipment.management.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("device")
/** 设备主数据，保存设备标识、分类、负责人和当前生命周期状态。 */
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
