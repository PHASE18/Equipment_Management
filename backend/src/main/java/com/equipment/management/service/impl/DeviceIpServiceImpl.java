package com.equipment.management.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.equipment.management.common.constant.ErrorCode;
import com.equipment.management.common.exception.BusinessException;
import com.equipment.management.entity.DeviceIp;
import com.equipment.management.mapper.DeviceIpMapper;
import com.equipment.management.common.service.impl.BaseCrudServiceImpl;
import com.equipment.management.service.DeviceIpService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class DeviceIpServiceImpl extends BaseCrudServiceImpl<DeviceIpMapper, DeviceIp> implements DeviceIpService {

    @Override
    public List<DeviceIp> listByDeviceId(Long deviceId) {
        return list(Wrappers.<DeviceIp>lambdaQuery().eq(DeviceIp::getDeviceId, deviceId));
    }

    @Override
    public void createEntity(DeviceIp entity) {
        validateIpUnique(entity, null);
        super.createEntity(entity);
    }

    @Override
    public void updateEntity(DeviceIp entity) {
        validateIpUnique(entity, entity.getId());
        super.updateEntity(entity);
    }

    private void validateIpUnique(DeviceIp entity, Long excludeId) {
        if (StringUtils.hasText(entity.getBusinessIp())) {
            long count = count(Wrappers.<DeviceIp>lambdaQuery()
                    .eq(DeviceIp::getBusinessIp, entity.getBusinessIp())
                    .ne(excludeId != null, DeviceIp::getId, excludeId));
            if (count > 0) {
                throw new BusinessException(ErrorCode.BUSINESS_IP_DUPLICATE);
            }
        }
        if (StringUtils.hasText(entity.getManagementIp())) {
            long count = count(Wrappers.<DeviceIp>lambdaQuery()
                    .eq(DeviceIp::getManagementIp, entity.getManagementIp())
                    .ne(excludeId != null, DeviceIp::getId, excludeId));
            if (count > 0) {
                throw new BusinessException(ErrorCode.MANAGEMENT_IP_DUPLICATE);
            }
        }
    }
}
