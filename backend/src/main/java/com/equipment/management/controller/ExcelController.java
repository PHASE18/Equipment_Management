package com.equipment.management.controller;

import com.equipment.management.annotation.RequireAuth;
import com.equipment.management.common.result.Result;
import com.equipment.management.dto.request.DeviceQuery;
import com.equipment.management.dto.response.ExcelImportResponse;
import com.equipment.management.service.ExcelService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequireAuth
@RequestMapping("/api/excel")
@RequiredArgsConstructor
public class ExcelController {

    private final ExcelService excelService;

    @PostMapping("/import")
    public Result<ExcelImportResponse> importDevices(@RequestParam("file") MultipartFile file) {
        return Result.success(excelService.importDevices(file));
    }

    @GetMapping("/export")
    public void exportDevices(DeviceQuery query, HttpServletResponse response) {
        excelService.exportDevices(query, response);
    }

    @GetMapping("/template")
    public void downloadTemplate(HttpServletResponse response) {
        excelService.downloadTemplate(response);
    }
}
