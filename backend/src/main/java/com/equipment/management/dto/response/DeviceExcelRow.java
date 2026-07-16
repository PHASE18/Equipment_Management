package com.equipment.management.dto.response;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.time.LocalDate;

@Data
public class DeviceExcelRow {

    @ExcelProperty("设备编号")
    private String deviceNo;

    @ExcelProperty("设备名称")
    private String deviceName;

    @ExcelProperty("SN号")
    private String sn;

    @ExcelProperty("资产编号")
    private String assetNo;

    @ExcelProperty("品牌编码")
    private String brandCode;

    @ExcelProperty("型号")
    private String model;

    @ExcelProperty("设备类型编码")
    private String deviceTypeCode;

    @ExcelProperty("部门ID")
    private Long departmentId;

    @ExcelProperty("设备状态")
    private String statusCode;

    @ExcelProperty("机柜位置")
    private String cabinet;

    @ExcelProperty("物理位置")
    private String location;

    @ExcelProperty("供应商")
    private String supplier;

    @ExcelProperty("维保单位")
    private String maintenanceCompany;

    @ExcelProperty("采购日期")
    private LocalDate purchaseDate;

    @ExcelProperty("保修截止")
    private LocalDate warrantyEnd;

    @ExcelProperty("备注")
    private String remark;
}
