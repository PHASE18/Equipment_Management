package com.equipment.management.service;

import com.equipment.management.dto.response.FileUploadResponse;
import com.equipment.management.entity.DeviceAttachment;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileService {

    FileUploadResponse upload(MultipartFile file, Long deviceId, String fileTypeCode);

    void download(Long id, HttpServletResponse response);

    void delete(Long id);

    List<DeviceAttachment> listByDeviceId(Long deviceId);
}
