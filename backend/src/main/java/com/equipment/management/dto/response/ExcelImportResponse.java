package com.equipment.management.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExcelImportResponse {

    private int successCount;
    private int failCount;
    private String message;
}
