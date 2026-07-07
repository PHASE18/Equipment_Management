package com.equipment.management.service;

import com.equipment.management.dto.request.DeviceQuery;
import com.equipment.management.dto.response.ExcelImportResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

public interface ExcelService {

    ExcelImportResponse importDevices(MultipartFile file);

    void exportDevices(DeviceQuery query, HttpServletResponse response);

    void downloadTemplate(HttpServletResponse response);
}
