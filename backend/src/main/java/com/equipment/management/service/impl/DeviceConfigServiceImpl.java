package com.equipment.management.service.impl;

import com.equipment.management.entity.DeviceConfig;
import com.equipment.management.mapper.DeviceConfigMapper;
import com.equipment.management.common.service.impl.BaseCrudServiceImpl;
import com.equipment.management.service.DeviceConfigService;
import org.springframework.stereotype.Service;

@Service
public class DeviceConfigServiceImpl extends BaseCrudServiceImpl<DeviceConfigMapper, DeviceConfig>
        implements DeviceConfigService {
}
