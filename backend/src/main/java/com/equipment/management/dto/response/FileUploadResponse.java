package com.equipment.management.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class FileUploadResponse {

    private Long fileId;
    private Long deviceId;
    private String fileName;
    private String fileTypeCode;
    private String category;
    private Long fileSize;
    /** MinIO 对象路径，数据库持久化字段 */
    private String filePath;
    /** 访问地址（由对象路径拼接） */
    private String url;
    private LocalDateTime uploadTime;
}
