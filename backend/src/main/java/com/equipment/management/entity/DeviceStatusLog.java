package com.equipment.management.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("device_status_log")
public class DeviceStatusLog {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long deviceId;
    private String oldStatusCode;
    private String newStatusCode;
    private String changeReason;
    private Long operatorId;
    private LocalDateTime changeTime;
}
