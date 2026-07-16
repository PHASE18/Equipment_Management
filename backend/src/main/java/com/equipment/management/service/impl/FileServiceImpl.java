package com.equipment.management.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.equipment.management.common.constant.ErrorCode;
import com.equipment.management.common.context.UserContext;
import com.equipment.management.common.enums.FileCategory;
import com.equipment.management.common.exception.BusinessException;
import com.equipment.management.common.util.FileUploadValidator;
import com.equipment.management.common.util.MinioUtils;
import com.equipment.management.dto.response.FileUploadResponse;
import com.equipment.management.entity.Device;
import com.equipment.management.entity.DeviceAttachment;
import com.equipment.management.mapper.DeviceAttachmentMapper;
import com.equipment.management.mapper.DeviceMapper;
import com.equipment.management.service.FileService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final MinioUtils minioUtils;
    private final FileUploadValidator fileUploadValidator;
    private final DeviceMapper deviceMapper;
    private final DeviceAttachmentMapper deviceAttachmentMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FileUploadResponse upload(MultipartFile file, Long deviceId, String category, String fileTypeCode) {
        return upload(file, deviceId, null, category, fileTypeCode);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FileUploadResponse upload(MultipartFile file, Long deviceId, Long maintenanceId,
                                     String category, String fileTypeCode) {
        if (deviceId == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "设备ID不能为空");
        }
        Device device = deviceMapper.selectById(deviceId);
        if (device == null) {
            throw new BusinessException(ErrorCode.DEVICE_NOT_FOUND);
        }

        FileCategory fileCategory = FileCategory.fromCode(category);
        fileUploadValidator.validate(file, fileCategory);

        String resolvedTypeCode = StringUtils.hasText(fileTypeCode)
                ? fileTypeCode
                : fileCategory.getDefaultFileTypeCode();
        String directory = fileCategory.getCode() + "/device/" + deviceId;
        String objectName = minioUtils.upload(file, directory);

        DeviceAttachment attachment = new DeviceAttachment();
        attachment.setDeviceId(deviceId);
        attachment.setMaintenanceId(maintenanceId);
        attachment.setFileName(file.getOriginalFilename());
        attachment.setFileTypeCode(resolvedTypeCode);
        attachment.setFileSize(file.getSize());
        attachment.setFilePath(objectName);
        attachment.setUploadUserId(UserContext.getUserId());
        attachment.setUploadTime(LocalDateTime.now());
        deviceAttachmentMapper.insert(attachment);

        log.info("用户 {} 上传附件 deviceId={}, category={}, path={}",
                UserContext.getUsername(), deviceId, fileCategory.getCode(), objectName);
        return toResponse(attachment, fileCategory.getCode());
    }

    @Override
    public void download(Long id, HttpServletResponse response) {
        DeviceAttachment attachment = getAttachmentOrThrow(id);
        try (InputStream inputStream = minioUtils.download(attachment.getFilePath());
             OutputStream outputStream = response.getOutputStream()) {
            String contentType = resolveContentType(attachment.getFileName());
            String encodedName = URLEncoder.encode(attachment.getFileName(), StandardCharsets.UTF_8)
                    .replace("+", "%20");
            response.setContentType(contentType);
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedName);
            if (attachment.getFileSize() != null) {
                response.setContentLengthLong(attachment.getFileSize());
            }
            inputStream.transferTo(outputStream);
            outputStream.flush();
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "文件下载失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        DeviceAttachment attachment = getAttachmentOrThrow(id);
        deviceAttachmentMapper.deleteById(id);
        try {
            minioUtils.delete(attachment.getFilePath());
        } catch (BusinessException ex) {
            log.warn("删除 MinIO 对象失败, path={}", attachment.getFilePath(), ex);
        }
    }

    @Override
    public List<FileUploadResponse> listByDeviceId(Long deviceId) {
        if (deviceId == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "设备ID不能为空");
        }
        return deviceAttachmentMapper.selectList(Wrappers.<DeviceAttachment>lambdaQuery()
                        .eq(DeviceAttachment::getDeviceId, deviceId)
                        .orderByDesc(DeviceAttachment::getUploadTime))
                .stream()
                .map(item -> toResponse(item, null))
                .toList();
    }

    @Override
    public List<FileUploadResponse> listByMaintenanceId(Long maintenanceId) {
        if (maintenanceId == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "维修工单ID不能为空");
        }
        return deviceAttachmentMapper.selectList(Wrappers.<DeviceAttachment>lambdaQuery()
                        .eq(DeviceAttachment::getMaintenanceId, maintenanceId)
                        .orderByDesc(DeviceAttachment::getUploadTime))
                .stream()
                .map(item -> toResponse(item, null))
                .toList();
    }

    private DeviceAttachment getAttachmentOrThrow(Long id) {
        DeviceAttachment attachment = deviceAttachmentMapper.selectById(id);
        if (attachment == null) {
            throw new BusinessException(ErrorCode.ATTACHMENT_NOT_FOUND);
        }
        return attachment;
    }

    private FileUploadResponse toResponse(DeviceAttachment attachment, String category) {
        return FileUploadResponse.builder()
                .fileId(attachment.getId())
                .deviceId(attachment.getDeviceId())
                .maintenanceId(attachment.getMaintenanceId())
                .fileName(attachment.getFileName())
                .fileTypeCode(attachment.getFileTypeCode())
                .category(category)
                .fileSize(attachment.getFileSize())
                .filePath(attachment.getFilePath())
                .url(minioUtils.getObjectUrl(attachment.getFilePath()))
                .uploadTime(attachment.getUploadTime())
                .build();
    }

    private String resolveContentType(String fileName) {
        String extension = fileUploadValidator.resolveExtension(fileName);
        return switch (extension) {
            case "jpg", "jpeg" -> MediaType.IMAGE_JPEG_VALUE;
            case "png" -> MediaType.IMAGE_PNG_VALUE;
            case "gif" -> MediaType.IMAGE_GIF_VALUE;
            case "webp" -> "image/webp";
            case "pdf" -> MediaType.APPLICATION_PDF_VALUE;
            case "doc" -> "application/msword";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls" -> "application/vnd.ms-excel";
            case "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "txt" -> MediaType.TEXT_PLAIN_VALUE;
            default -> MediaType.APPLICATION_OCTET_STREAM_VALUE;
        };
    }
}
