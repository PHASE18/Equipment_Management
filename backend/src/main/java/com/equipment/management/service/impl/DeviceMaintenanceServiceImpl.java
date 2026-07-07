package com.equipment.management.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.equipment.management.common.result.PageResult;
import com.equipment.management.dto.request.MaintenanceQuery;
import com.equipment.management.entity.DeviceMaintenance;
import com.equipment.management.mapper.DeviceMaintenanceMapper;
import com.equipment.management.common.service.impl.BaseCrudServiceImpl;
import com.equipment.management.service.DeviceMaintenanceService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class DeviceMaintenanceServiceImpl extends BaseCrudServiceImpl<DeviceMaintenanceMapper, DeviceMaintenance>
        implements DeviceMaintenanceService {

    @Override
    public PageResult<DeviceMaintenance> page(MaintenanceQuery query) {
        LambdaQueryWrapper<DeviceMaintenance> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(query.getDeviceId() != null, DeviceMaintenance::getDeviceId, query.getDeviceId())
                .eq(StringUtils.hasText(query.getFaultType()), DeviceMaintenance::getFaultTypeCode, query.getFaultType());
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(DeviceMaintenance::getMaintenancePerson, query.getKeyword())
                    .or().like(DeviceMaintenance::getFaultDescription, query.getKeyword()));
        }
        return pageQuery(query, wrapper);
    }
}
