package com.equipment.management.service.impl;

import com.equipment.management.common.constant.ErrorCode;
import com.equipment.management.common.exception.BusinessException;
import com.equipment.management.common.util.FileUploadValidator;
import com.equipment.management.common.util.MinioUtils;
import com.equipment.management.dto.response.FileUploadResponse;
import com.equipment.management.entity.Device;
import com.equipment.management.entity.DeviceAttachment;
import com.equipment.management.mapper.DeviceAttachmentMapper;
import com.equipment.management.mapper.DeviceMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileServiceImplTest {

    @Mock
    private MinioUtils minioUtils;
    @Mock
    private FileUploadValidator fileUploadValidator;
    @Mock
    private DeviceMapper deviceMapper;
    @Mock
    private DeviceAttachmentMapper deviceAttachmentMapper;

    private FileServiceImpl fileService;

    @BeforeEach
    void setUp() {
        fileService = new FileServiceImpl(minioUtils, fileUploadValidator, deviceMapper, deviceAttachmentMapper);
    }

    @Test
    @DisplayName("上传：设备不存在应抛出 DEVICE_NOT_FOUND")
    void upload_deviceNotFound() {
        when(deviceMapper.selectById(99L)).thenReturn(null);
        MockMultipartFile file = new MockMultipartFile("file", "a.pdf", "application/pdf", "x".getBytes());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> fileService.upload(file, 99L, "document", null));
        assertEquals(ErrorCode.DEVICE_NOT_FOUND.getCode(), ex.getCode());
        verify(minioUtils, never()).upload(any(), anyString());
    }

    @Test
    @DisplayName("上传：设备ID为空应抛出 BAD_REQUEST")
    void upload_nullDeviceId() {
        MockMultipartFile file = new MockMultipartFile("file", "a.pdf", "application/pdf", "x".getBytes());
        BusinessException ex = assertThrows(BusinessException.class,
                () -> fileService.upload(file, null, "document", null));
        assertEquals(ErrorCode.BAD_REQUEST.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("上传成功应写入附件元数据并返回响应")
    void upload_success() {
        Device device = new Device();
        device.setId(1L);
        when(deviceMapper.selectById(1L)).thenReturn(device);
        when(minioUtils.upload(any(), eq("document/device/1"))).thenReturn("document/device/1/2026/7/uuid.pdf");
        when(minioUtils.getObjectUrl(anyString())).thenReturn("http://local/uuid.pdf");
        when(deviceAttachmentMapper.insert(any(DeviceAttachment.class))).thenAnswer(invocation -> {
            DeviceAttachment attachment = invocation.getArgument(0);
            attachment.setId(10L);
            return 1;
        });

        MockMultipartFile file = new MockMultipartFile("file", "report.pdf", "application/pdf", "pdf".getBytes());
        FileUploadResponse response = fileService.upload(file, 1L, "document", "OTHER_DOC");

        assertEquals(10L, response.getFileId());
        assertEquals(1L, response.getDeviceId());
        assertEquals("report.pdf", response.getFileName());
        assertEquals("OTHER_DOC", response.getFileTypeCode());
        assertEquals("document", response.getCategory());

        ArgumentCaptor<DeviceAttachment> captor = ArgumentCaptor.forClass(DeviceAttachment.class);
        verify(deviceAttachmentMapper).insert(captor.capture());
        assertEquals("report.pdf", captor.getValue().getFileName());
        verify(fileUploadValidator).validate(any(), any());
    }

    @Test
    @DisplayName("删除：附件不存在应抛出 ATTACHMENT_NOT_FOUND")
    void delete_notFound() {
        when(deviceAttachmentMapper.selectById(8L)).thenReturn(null);
        BusinessException ex = assertThrows(BusinessException.class, () -> fileService.delete(8L));
        assertEquals(ErrorCode.ATTACHMENT_NOT_FOUND.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("删除成功应删除 DB 记录并清理存储对象")
    void delete_success() {
        DeviceAttachment attachment = new DeviceAttachment();
        attachment.setId(8L);
        attachment.setFilePath("document/device/1/a.pdf");
        when(deviceAttachmentMapper.selectById(8L)).thenReturn(attachment);
        when(deviceAttachmentMapper.deleteById(8L)).thenReturn(1);
        doNothing().when(minioUtils).delete("document/device/1/a.pdf");

        fileService.delete(8L);

        verify(deviceAttachmentMapper).deleteById(8L);
        verify(minioUtils).delete("document/device/1/a.pdf");
    }

    @Test
    @DisplayName("下载应将文件流写入响应")
    void download_success() throws Exception {
        DeviceAttachment attachment = new DeviceAttachment();
        attachment.setId(3L);
        attachment.setFileName("photo.png");
        attachment.setFilePath("image/device/1/a.png");
        attachment.setFileSize(4L);
        when(deviceAttachmentMapper.selectById(3L)).thenReturn(attachment);
        when(minioUtils.download("image/device/1/a.png"))
                .thenReturn(new ByteArrayInputStream("png!".getBytes(StandardCharsets.UTF_8)));
        when(fileUploadValidator.resolveExtension("photo.png")).thenReturn("png");

        MockHttpServletResponse response = new MockHttpServletResponse();
        fileService.download(3L, response);

        assertEquals("image/png", response.getContentType());
        assertTrue(response.getHeader("Content-Disposition").contains("photo.png")
                || response.getHeader("Content-Disposition").contains("photo"));
        assertEquals("png!", response.getContentAsString());
    }

    @Test
    @DisplayName("批量上传空数组应失败")
    void batchUpload_empty() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> fileService.batchUpload(new MockMultipartFile[0], 1L, null, "document", null));
        assertEquals(ErrorCode.BAD_REQUEST.getCode(), ex.getCode());
    }
}
