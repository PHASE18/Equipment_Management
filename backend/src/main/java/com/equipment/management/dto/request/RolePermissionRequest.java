package com.equipment.management.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RolePermissionRequest {

    @NotNull(message = "角色ID不能为空")
    private Long roleId;

    @NotNull(message = "权限ID列表不能为空")
    private java.util.List<Long> permissionIds;
}
