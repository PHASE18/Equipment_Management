package com.equipment.management.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
/** 系统角色，作为用户权限集合的载体。 */
public class SysRole extends BaseEntity {

    private String roleName;
    private String roleCode;
    private String remark;
}
