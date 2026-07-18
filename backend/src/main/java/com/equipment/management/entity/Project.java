package com.equipment.management.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("project")
/** 设备所属项目或业务现场的基础信息。 */
public class Project extends BaseEntity {

    private String projectName;
    private String projectCode;
    private Long departmentId;
    private String remark;
}
