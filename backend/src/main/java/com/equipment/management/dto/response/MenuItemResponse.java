package com.equipment.management.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MenuItemResponse {

    private Long id;
    private Long parentId;
    private String title;
    private String path;
    private String permissionCode;
    private String icon;
    private List<MenuItemResponse> children;
}
