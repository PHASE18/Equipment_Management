package com.equipment.management.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
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
    /** 是否固定资产：0-否 1-是 */
    private Integer isFixedAsset;
    private String brandCode;
    private String model;
    private String deviceTypeCode;
    /** 管理部门 */
    private Long departmentId;
    /** 使用部门 */
    private Long useDepartmentId;
    /** 责任人用户ID（数据权限 SELF 仍可用，表单不再编辑） */
    private Long managerUserId;
    /** 责任人（自由文本） */
    private String managerName;
    /** 使用人 */
    private String useUserName;
    private BigDecimal originalValue;
    private String approvalNo;
    private String supplier;
    private String maintenanceCompany;
    private LocalDate purchaseDate;
    private LocalDate manufactureDate;
    private LocalDate onlineDate;
    /** 到保日期 */
    private LocalDate warrantyEnd;
    private LocalDate scrapDate;
    private String statusCode;
    /** 是否维修中：0-否 1-是（可与在用/停用并存） */
    private Integer maintainingFlag;
    /** 机柜U位 */
    private String cabinet;
    /** 所在机房 */
    private String location;
    private String remark;
}
