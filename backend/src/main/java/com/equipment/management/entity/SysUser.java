package com.equipment.management.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
/** 系统用户及其登录、部门和启用状态信息。 */
public class SysUser extends BaseEntity {

    private String username;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    private String realName;
    private Long departmentId;
    private String phone;
    private String email;
    private Integer status;
}
