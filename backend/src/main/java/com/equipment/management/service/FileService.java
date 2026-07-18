package com.equipment.management.service;

import com.equipment.management.dto.response.FileUploadResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/** 文件对象存储和附件元数据领域服务。 */
public interface FileService {

    FileUploadResponse upload(MultipartFile file, Long deviceId, String category, String fileTypeCode);

    FileUploadResponse upload(MultipartFile file, Long deviceId, Long maintenanceId, String category, String fileTypeCode);

    void download(Long id, HttpServletResponse response);

    void delete(Long id);

    List<FileUploadResponse> listByDeviceId(Long deviceId);

    List<FileUploadResponse> listByMaintenanceId(Long maintenanceId);

    List<FileUploadResponse> batchUpload(MultipartFile[] files, Long deviceId, Long maintenanceId,
                                         String category, String fileTypeCode);
}
