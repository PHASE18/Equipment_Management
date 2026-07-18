package com.equipment.management.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_department")
/** 组织部门实体，parentId 用于构成部门树。 */
public class SysDepartment extends BaseEntity {

    private String departmentName;
    private Long parentId;
    private String leader;
    private String phone;
    private String remark;
}
