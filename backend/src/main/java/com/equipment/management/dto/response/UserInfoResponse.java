package com.equipment.management.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserInfoResponse {

    private Long id;
    private String name;
    private String department;
    private String username;
    private Long departmentId;
}
