package com.equipment.management.service.impl;

import com.equipment.management.common.util.ExcelUtils;
import com.equipment.management.dto.request.DeviceQuery;
import com.equipment.management.dto.response.ExcelImportResponse;
import com.equipment.management.service.ExcelService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class ExcelServiceImpl implements ExcelService {

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
        // TODO: EasyExcel 按条件导出设备数据
        ExcelUtils.setDownloadHeader(response, "devices.xlsx");
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) {
        // TODO: 输出 Excel 导入模板
        ExcelUtils.setDownloadHeader(response, "device_import_template.xlsx");
    }

}
