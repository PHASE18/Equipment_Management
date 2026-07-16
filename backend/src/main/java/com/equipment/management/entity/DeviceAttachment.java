package com.equipment.management.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("device_attachment")
public class DeviceAttachment extends BaseEntity {

    private Long deviceId;
    private Long maintenanceId;
    private String fileName;
    private String fileTypeCode;
    private Long fileSize;
    private String filePath;
    private Long uploadUserId;
    private LocalDateTime uploadTime;
}
