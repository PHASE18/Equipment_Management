package com.equipment.management.service;

import com.equipment.management.common.result.PageResult;
import com.equipment.management.common.service.BaseCrudService;
import com.equipment.management.dto.request.DeviceQuery;
import com.equipment.management.entity.Device;

/** 设备主数据领域服务。 */
public interface DeviceService extends BaseCrudService<Device> {

    PageResult<Device> page(DeviceQuery query);
}
