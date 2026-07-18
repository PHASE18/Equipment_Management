package com.equipment.management.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("device_config")
/** 设备扩展配置项，按设备保存可变的配置名称和值。 */
public class DeviceConfig extends BaseEntity {

    private Long deviceId;
    private String cpu;
    private String memory;
    private String disk;
    private String raid;
    private String os;
    private String firmware;
    private String bios;
    private String remark;
}
