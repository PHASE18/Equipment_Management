package com.equipment.management.dto.response;

import lombok.Builder;
import lombok.Data;

/** 业务下拉用的用户选项（不含密码等敏感字段）。 */
@Data
@Builder
public class UserOptionResponse {

    private Long id;
    private String username;
    private String realName;
    private Long departmentId;
    private Integer status;
}
