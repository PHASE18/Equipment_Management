package com.equipment.management.dto.response;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
/** 设备档案导出行：可读中文列，含基本信息 / 配置 / 网络 / 所属项目。 */
public class DeviceExcelRow {

    @ExcelProperty("设备编号")
    private String deviceNo;

    @ExcelProperty("设备名称")
    private String deviceName;

    @ExcelProperty("SN号")
    private String sn;

    @ExcelProperty("资产编号")
    private String assetNo;

    @ExcelProperty("是否固定资产")
    private String isFixedAsset;

    @ExcelProperty("品牌")
    private String brandName;

    @ExcelProperty("型号")
    private String model;

    @ExcelProperty("设备类型")
    private String deviceTypeName;

    @ExcelProperty("设备状态")
    private String statusName;

    @ExcelProperty("管理部门")
    private String departmentName;

    @ExcelProperty("使用部门")
    private String useDepartmentName;

    @ExcelProperty("责任人")
    private String managerName;

    @ExcelProperty("使用人")
    private String useUserName;

    @ExcelProperty("设备原值")
    private String originalValue;

    @ExcelProperty("批准文号")
    private String approvalNo;

    @ExcelProperty("供应商")
    private String supplier;

    @ExcelProperty("维保单位")
    private String maintenanceCompany;

    @ExcelProperty("采购日期")
    private String purchaseDate;

    @ExcelProperty("出厂日期")
    private String manufactureDate;

    @ExcelProperty("上架日期")
    private String onlineDate;

    @ExcelProperty("到保日期")
    private String warrantyEnd;

    @ExcelProperty("报废日期")
    private String scrapDate;

    @ExcelProperty("所在机房")
    private String location;

    @ExcelProperty("机柜U位")
    private String cabinet;

    @ExcelProperty("所属项目")
    private String projectNames;

    @ExcelProperty("CPU")
    private String cpu;

    @ExcelProperty("内存")
    private String memory;

    @ExcelProperty("硬盘")
    private String disk;

    @ExcelProperty("Raid")
    private String raid;

    @ExcelProperty("GPU")
    private String gpu;

    @ExcelProperty("光纤卡")
    private String fiberCard;

    @ExcelProperty("网卡")
    private String nic;

    @ExcelProperty("电源")
    private String powerSupply;

    @ExcelProperty("系统")
    private String os;

    @ExcelProperty("数据库版本")
    private String dbVersion;

    @ExcelProperty("固件版本")
    private String firmware;

    @ExcelProperty("BIOS")
    private String bios;

    @ExcelProperty("配置备注")
    private String configRemark;

    @ExcelProperty("业务IP")
    private String businessIp;

    @ExcelProperty("管理IP")
    private String managementIp;

    @ExcelProperty("子网掩码")
    private String mask;

    @ExcelProperty("网关")
    private String gateway;

    @ExcelProperty("挂载业务")
    private String mountedBusiness;

    @ExcelProperty("所属网络")
    private String networkZone;

    @ExcelProperty("管理地址登录方式")
    private String mgmtLoginMethod;

    @ExcelProperty("备注")
    private String remark;
}
