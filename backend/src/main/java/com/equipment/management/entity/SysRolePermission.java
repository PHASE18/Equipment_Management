package com.equipment.management.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("sys_role_permission")
/** 角色与权限的关联记录。 */
public class SysRolePermission implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long roleId;
    private Long permissionId;
    private LocalDateTime createTime;
}
