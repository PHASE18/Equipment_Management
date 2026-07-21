package com.equipment.management.common.util;

import com.alibaba.excel.EasyExcel;
import com.equipment.management.common.constant.ErrorCode;
import com.equipment.management.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

public final class ExcelUtils {

    private static final String XLS = "xls";
    private static final String XLSX = "xlsx";
    private static final String CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private ExcelUtils() {
    }

    public static void validateExcelFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.EXCEL_FORMAT_ERROR, "Excel file cannot be empty");
        }
        String filename = file.getOriginalFilename();
        if (!StringUtils.hasText(filename) || !filename.contains(".")) {
            throw new BusinessException(ErrorCode.EXCEL_FORMAT_ERROR, "Excel filename is invalid");
        }
        String ext = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        if (!XLSX.equals(ext) && !XLS.equals(ext)) {
            throw new BusinessException(ErrorCode.EXCEL_FORMAT_ERROR, "Only xlsx/xls files are supported");
        }
    }

    public static <T> List<T> read(MultipartFile file, Class<T> headClass) {
        validateExcelFile(file);
        try {
            return EasyExcel.read(file.getInputStream())
                    .head(headClass)
                    .sheet()
                    .doReadSync();
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.EXCEL_IMPORT_FAILED, "Failed to read Excel file");
        }
    }

    public static <T> void write(HttpServletResponse response, String filename, String sheetName,
                                 Class<T> headClass, List<T> data) {
        try {
            // Write to memory first so failures return JSON instead of a broken xlsx stream.
            java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
            EasyExcel.write(buffer, headClass)
                    .sheet(StringUtils.hasText(sheetName) ? sheetName : "sheet1")
                    .doWrite(data);
            byte[] bytes = buffer.toByteArray();
            setDownloadHeader(response, filename);
            response.setContentLength(bytes.length);
            response.getOutputStream().write(bytes);
            response.getOutputStream().flush();
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Failed to write Excel file");
        } catch (RuntimeException e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Failed to generate Excel: " + e.getMessage());
        }
    }

    public static void setDownloadHeader(HttpServletResponse response, String filename) {
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
        response.setContentType(CONTENT_TYPE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFilename);
    }
}
