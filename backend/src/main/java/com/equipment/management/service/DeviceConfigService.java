package com.equipment.management.service;

import com.equipment.management.common.service.BaseCrudService;
import com.equipment.management.entity.DeviceConfig;

public interface DeviceConfigService extends BaseCrudService<DeviceConfig> {

    DeviceConfig getByDeviceId(Long deviceId);

    void saveByDeviceId(DeviceConfig entity);
}
