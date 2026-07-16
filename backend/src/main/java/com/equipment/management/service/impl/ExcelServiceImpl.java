package com.equipment.management.service.impl;

import com.equipment.management.common.result.PageResult;
import com.equipment.management.common.util.ExcelUtils;
import com.equipment.management.dto.request.DeviceQuery;
import com.equipment.management.dto.response.DeviceExcelRow;
import com.equipment.management.dto.response.ExcelImportResponse;
import com.equipment.management.entity.Device;
import com.equipment.management.service.DeviceService;
import com.equipment.management.service.ExcelService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelServiceImpl implements ExcelService {

    private static final long EXPORT_MAX_SIZE = 5000L;

    private final DeviceService deviceService;

    @Override
    public ExcelImportResponse importDevices(MultipartFile file) {
        ExcelUtils.validateExcelFile(file);
        // TODO: EasyExcel 解析并批量导入，校验 SN/设备编号唯一性
        return ExcelImportResponse.builder()
                .successCount(0)
                .failCount(0)
                .message("导入完成")
                .build();
    }

    @Override
    public void exportDevices(DeviceQuery query, HttpServletResponse response) {
        query.setPageNum(1);
        query.setPageSize(EXPORT_MAX_SIZE);
        PageResult<Device> page = deviceService.page(query);
        List<DeviceExcelRow> rows = page.getRecords().stream()
                .map(this::toExcelRow)
                .toList();
        ExcelUtils.write(response, "devices.xlsx", "设备档案", DeviceExcelRow.class, rows);
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) {
        ExcelUtils.write(response, "device_import_template.xlsx", "设备档案",
                DeviceExcelRow.class, List.of());
    }

    private DeviceExcelRow toExcelRow(Device device) {
        DeviceExcelRow row = new DeviceExcelRow();
        row.setDeviceNo(device.getDeviceNo());
        row.setDeviceName(device.getDeviceName());
        row.setSn(device.getSn());
        row.setAssetNo(device.getAssetNo());
        row.setBrandCode(device.getBrandCode());
        row.setModel(device.getModel());
        row.setDeviceTypeCode(device.getDeviceTypeCode());
        row.setDepartmentId(device.getDepartmentId());
        row.setStatusCode(device.getStatusCode());
        row.setCabinet(device.getCabinet());
        row.setLocation(device.getLocation());
        row.setSupplier(device.getSupplier());
        row.setMaintenanceCompany(device.getMaintenanceCompany());
        row.setPurchaseDate(device.getPurchaseDate());
        row.setWarrantyEnd(device.getWarrantyEnd());
        row.setRemark(device.getRemark());
        return row;
    }
}
