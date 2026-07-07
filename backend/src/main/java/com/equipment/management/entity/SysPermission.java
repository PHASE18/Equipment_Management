package com.equipment.management.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_permission")
public class SysPermission extends BaseEntity {

    private Long parentId;
    private String permissionName;
    private String permissionCode;
    private Integer permissionType;
    private String path;
    private String icon;
    private Integer sort;
    private Integer status;
}
