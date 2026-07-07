package com.equipment.management.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.equipment.management.common.result.PageResult;
import com.equipment.management.dto.request.LogQuery;
import com.equipment.management.entity.SysOperationLog;

public interface SysOperationLogService extends IService<SysOperationLog> {

    PageResult<SysOperationLog> pageQuery(LogQuery query);

    SysOperationLog getDetail(Long id);

    void removeLog(Long id);
}
