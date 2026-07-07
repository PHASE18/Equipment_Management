package com.equipment.management.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.equipment.management.common.result.PageResult;
import com.equipment.management.dto.request.LogQuery;
import com.equipment.management.entity.DeviceStatusLog;

public interface DeviceStatusLogService extends IService<DeviceStatusLog> {

    PageResult<DeviceStatusLog> pageQuery(LogQuery query);

    DeviceStatusLog getDetail(Long id);

    void removeLog(Long id);
}
