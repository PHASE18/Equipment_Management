package com.equipment.management.service.impl;

import com.equipment.management.common.constant.ErrorCode;
import com.equipment.management.common.exception.BusinessException;
import com.equipment.management.dto.request.DeviceQuery;
import com.equipment.management.dto.response.ExcelImportResponse;
import com.equipment.management.service.ExcelService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;

@Slf4j
@Service
public class ExcelServiceImpl implements ExcelService {

    @Override
    public ExcelImportResponse importDevices(MultipartFile file) {
        validateExcelFile(file);
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
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=devices.xlsx");
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) {
        // TODO: 输出 Excel 导入模板
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=device_import_template.xlsx");
    }

    private void validateExcelFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.EXCEL_FORMAT_ERROR, "Excel文件不能为空");
        }
        String filename = file.getOriginalFilename();
        if (!StringUtils.hasText(filename)) {
            throw new BusinessException(ErrorCode.EXCEL_FORMAT_ERROR);
        }
        String ext = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        if (!"xlsx".equals(ext) && !"xls".equals(ext)) {
            throw new BusinessException(ErrorCode.EXCEL_FORMAT_ERROR, "仅支持 xlsx/xls 格式");
        }
    }
}
