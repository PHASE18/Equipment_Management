package com.equipment.management.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.equipment.management.common.constant.ErrorCode;
import com.equipment.management.common.exception.BusinessException;
import com.equipment.management.common.result.PageResult;
import com.equipment.management.common.util.PageUtils;
import com.equipment.management.dto.request.LogQuery;
import com.equipment.management.entity.DeviceStatusLog;
import com.equipment.management.mapper.DeviceStatusLogMapper;
import com.equipment.management.service.DeviceStatusLogService;
import org.springframework.stereotype.Service;

@Service
public class DeviceStatusLogServiceImpl extends ServiceImpl<DeviceStatusLogMapper, DeviceStatusLog>
        implements DeviceStatusLogService {

    @Override
    public PageResult<DeviceStatusLog> pageQuery(LogQuery query) {
        LambdaQueryWrapper<DeviceStatusLog> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(query.getDeviceId() != null, DeviceStatusLog::getDeviceId, query.getDeviceId())
                .orderByDesc(DeviceStatusLog::getChangeTime);
        return PageUtils.toPageResult(page(PageUtils.buildPage(query), wrapper));
    }

    @Override
    public DeviceStatusLog getDetail(Long id) {
        DeviceStatusLog log = getById(id);
        if (log == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        return log;
    }

    @Override
    public void removeLog(Long id) {
        if (!removeById(id)) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
    }
}
