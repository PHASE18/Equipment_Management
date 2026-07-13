package com.equipment.management.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UserInfoResponse {

    private Long id;
    private String name;
    private String department;
    private String username;
    private Long departmentId;
    private List<String> roles;
    private List<String> permissions;
    private List<MenuItemResponse> menus;
    private String dataScope;
}
