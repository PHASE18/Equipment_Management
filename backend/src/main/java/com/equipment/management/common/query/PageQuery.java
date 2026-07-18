package com.equipment.management.common.query;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 统一分页查询参数
 */
@Data
/** 统一分页请求参数，负责页码、页大小和通用关键词承载。 */
public class PageQuery {

    @Min(value = 1, message = "页码最小为1")
    private long pageNum = 1;

    @Min(value = 1, message = "每页条数最小为1")
    @Max(value = 500, message = "每页条数最大为500")
    private long pageSize = 20;

    private String keyword;

    private String sortField = "createTime";

    private String sortOrder = "desc";
}
