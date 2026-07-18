package com.equipment.management.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("device_project")
/** 设备与项目之间的关联实体，用于表达多对多关系。 */
public class DeviceProject implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long deviceId;
    private Long projectId;
    private LocalDateTime createTime;
}
