package com.equipment.management.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("device_config")
/** 设备扩展配置项，按设备保存硬件与软件配置。 */
public class DeviceConfig extends BaseEntity {

    private Long deviceId;
    private String cpu;
    private String memory;
    private String disk;
    private String raid;
    private String gpu;
    private String fiberCard;
    private String nic;
    private String powerSupply;
    private String os;
    private String dbVersion;
    private String firmware;
    private String bios;
    private String remark;
}
