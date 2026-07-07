package com.equipment.management.service.impl;

import com.equipment.management.common.constant.ErrorCode;
import com.equipment.management.common.context.UserContext;
import com.equipment.management.common.exception.BusinessException;
import com.equipment.management.dto.response.FileUploadResponse;
import com.equipment.management.entity.DeviceAttachment;
import com.equipment.management.service.FileService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
public class FileServiceImpl implements FileService {

    @Value("${file.upload.allowed-extensions}")
    private String allowedExtensions;

    @Value("${file.upload.max-size}")
    private long maxSize;

    @Override
    public FileUploadResponse upload(MultipartFile file, Long deviceId, String fileTypeCode) {
        validateFile(file);
        // TODO: 上传至 MinIO，写入 device_attachment 元数据
        log.info("用户 {} 上传附件 deviceId={}, fileType={}", UserContext.getUsername(), deviceId, fileTypeCode);
        return FileUploadResponse.builder()
                .fileId(0L)
                .url("")
                .fileName(file.getOriginalFilename())
                .fileSize(file.getSize())
                .build();
    }

    @Override
    public void download(Long id, HttpServletResponse response) {
        // TODO: 从 MinIO 读取文件流写入 response
        throw new BusinessException(ErrorCode.ATTACHMENT_NOT_FOUND);
    }

    @Override
    public void delete(Long id) {
        // TODO: 逻辑删除附件记录，可选删除 MinIO 对象
    }

    @Override
    public List<DeviceAttachment> listByDeviceId(Long deviceId) {
        // TODO: 查询设备附件列表
        return Collections.emptyList();
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "上传文件不能为空");
        }
        if (file.getSize() > maxSize) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "文件大小超出限制");
        }
        String filename = file.getOriginalFilename();
        if (!StringUtils.hasText(filename) || !filename.contains(".")) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "文件格式不支持");
        }
        String ext = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        List<String> allowed = Arrays.asList(allowedExtensions.split(","));
        if (!allowed.contains(ext)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "不允许的文件类型: " + ext);
        }
    }
}
