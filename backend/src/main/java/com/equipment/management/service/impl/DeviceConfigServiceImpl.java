package com.equipment.management.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.equipment.management.common.constant.ErrorCode;
import com.equipment.management.common.exception.BusinessException;
import com.equipment.management.common.service.impl.BaseCrudServiceImpl;
import com.equipment.management.entity.DeviceConfig;
import com.equipment.management.mapper.DeviceConfigMapper;
import com.equipment.management.service.DeviceConfigService;
import org.springframework.stereotype.Service;

@Service
public class DeviceConfigServiceImpl extends BaseCrudServiceImpl<DeviceConfigMapper, DeviceConfig>
        implements DeviceConfigService {

    @Override
    public DeviceConfig getByDeviceId(Long deviceId) {
        return getOne(Wrappers.<DeviceConfig>lambdaQuery().eq(DeviceConfig::getDeviceId, deviceId));
    }

    @Override
    public void saveByDeviceId(DeviceConfig entity) {
        if (entity.getDeviceId() == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "设备ID不能为空");
        }
        DeviceConfig existing = getByDeviceId(entity.getDeviceId());
        if (existing != null) {
            entity.setId(existing.getId());
            updateEntity(entity);
            return;
        }
        createEntity(entity);
    }
}
