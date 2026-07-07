package com.equipment.management.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.equipment.management.common.result.PageResult;
import com.equipment.management.dto.request.LogQuery;
import com.equipment.management.entity.SysLoginLog;

public interface SysLoginLogService extends IService<SysLoginLog> {

    PageResult<SysLoginLog> pageQuery(LogQuery query);

    SysLoginLog getDetail(Long id);

    void removeLog(Long id);
}
