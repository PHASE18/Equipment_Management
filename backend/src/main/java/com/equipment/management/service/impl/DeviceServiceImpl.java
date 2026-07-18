package com.equipment.management.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.equipment.management.common.constant.ErrorCode;
import com.equipment.management.common.exception.BusinessException;
import com.equipment.management.common.result.PageResult;
import com.equipment.management.dto.request.DeviceQuery;
import com.equipment.management.entity.Device;
import com.equipment.management.mapper.DeviceMapper;
import com.equipment.management.common.service.impl.BaseCrudServiceImpl;
import com.equipment.management.service.DeviceService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
/** 设备主数据服务实现。 */
public class DeviceServiceImpl extends BaseCrudServiceImpl<DeviceMapper, Device> implements DeviceService {

    @Override
    public PageResult<Device> page(DeviceQuery query) {
        LambdaQueryWrapper<Device> wrapper = Wrappers.lambdaQuery();
        wrapper.like(StringUtils.hasText(query.getDeviceNo()), Device::getDeviceNo, query.getDeviceNo())
                .like(StringUtils.hasText(query.getDeviceName()), Device::getDeviceName, query.getDeviceName())
                .eq(StringUtils.hasText(query.getBrand()), Device::getBrandCode, query.getBrand())
                .like(StringUtils.hasText(query.getModel()), Device::getModel, query.getModel())
                .eq(StringUtils.hasText(query.getStatus()), Device::getStatusCode, query.getStatus())
                .eq(query.getDepartmentId() != null, Device::getDepartmentId, query.getDepartmentId());
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(Device::getDeviceNo, query.getKeyword())
                    .or().like(Device::getDeviceName, query.getKeyword())
                    .or().like(Device::getSn, query.getKeyword()));
        }
        return pageQuery(query, wrapper);
    }

    @Override
    public void createEntity(Device entity) {
        validateUnique(entity, null);
        super.createEntity(entity);
    }

    @Override
    public void updateEntity(Device entity) {
        validateUnique(entity, entity.getId());
        super.updateEntity(entity);
    }

    private void validateUnique(Device entity, Long excludeId) {
        if (StringUtils.hasText(entity.getDeviceNo())) {
            long count = count(Wrappers.<Device>lambdaQuery()
                    .eq(Device::getDeviceNo, entity.getDeviceNo())
                    .ne(excludeId != null, Device::getId, excludeId));
            if (count > 0) {
                throw new BusinessException(ErrorCode.DEVICE_NO_DUPLICATE);
            }
        }
        if (StringUtils.hasText(entity.getSn())) {
            long count = count(Wrappers.<Device>lambdaQuery()
                    .eq(Device::getSn, entity.getSn())
                    .ne(excludeId != null, Device::getId, excludeId));
            if (count > 0) {
                throw new BusinessException(ErrorCode.SN_DUPLICATE);
            }
        }
    }
}
