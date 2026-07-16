package com.equipment.management.service;

import com.equipment.management.common.service.BaseCrudService;
import com.equipment.management.entity.DeviceIp;

import java.util.List;

public interface DeviceIpService extends BaseCrudService<DeviceIp> {

    List<DeviceIp> listByDeviceId(Long deviceId);

    DeviceIp getByDeviceId(Long deviceId);

    void saveByDeviceId(DeviceIp entity);
}
