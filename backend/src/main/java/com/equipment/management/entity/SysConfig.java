package com.equipment.management.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_config")
/** 系统配置键值，供运行时读取可配置参数。 */
public class SysConfig extends BaseEntity {

    private String configKey;
    private String configValue;
    private String remark;
}
