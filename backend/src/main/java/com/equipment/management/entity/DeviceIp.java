package com.equipment.management.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("device_ip")
/** 设备网络地址记录，支持一台设备关联多个 IP。 */
public class DeviceIp extends BaseEntity {

    private Long deviceId;
    private String businessIp;
    private String managementIp;
    private String mask;
    private String gateway;
}
