package com.equipment.management.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserRoleRequest {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "角色ID列表不能为空")
    private java.util.List<Long> roleIds;
}
