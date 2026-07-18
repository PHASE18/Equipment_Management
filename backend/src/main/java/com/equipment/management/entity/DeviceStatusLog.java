package com.equipment.management.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("device_status_log")
/** 设备生命周期状态变更历史，保留变更前后状态和操作人。 */
public class DeviceStatusLog {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long deviceId;
    private String oldStatusCode;
    private String newStatusCode;
    private String changeReason;
    private String remark;
    private Long operatorId;
    private LocalDateTime changeTime;
}
