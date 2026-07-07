package com.equipment.management.dto.request;

import com.equipment.management.common.query.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class LogQuery extends PageQuery {

    private String username;
    private String operationType;
    private String tableName;
    private Long deviceId;
}
