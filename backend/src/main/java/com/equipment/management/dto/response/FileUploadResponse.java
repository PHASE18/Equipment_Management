package com.equipment.management.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FileUploadResponse {

    private Long fileId;
    private String url;
    private String fileName;
    private Long fileSize;
}
