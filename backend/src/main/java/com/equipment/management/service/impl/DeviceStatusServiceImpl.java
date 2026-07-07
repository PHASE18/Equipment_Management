package com.equipment.management.service.impl;

import com.equipment.management.common.constant.ErrorCode;
import com.equipment.management.common.context.UserContext;
import com.equipment.management.common.exception.BusinessException;
import com.equipment.management.common.query.PageQuery;
import com.equipment.management.common.result.PageResult;
import com.equipment.management.dto.request.DeviceStatusChangeRequest;
import com.equipment.management.entity.DeviceStatusLog;
import com.equipment.management.service.DeviceStatusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class DeviceStatusServiceImpl implements DeviceStatusService {

    @Override
    public PageResult<DeviceStatusLog> listByDeviceId(Long deviceId, PageQuery query) {
        // TODO: 分页查询设备生命周期日志
        return PageResult.empty(query.getPageNum(), query.getPageSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(DeviceStatusChangeRequest request) {
        // TODO: 1. 查询设备当前状态
        // TODO: 2. 校验状态流转合法性（采购中→库存→待上架→在用→维修中/备用→停用→报废）
        // TODO: 3. 更新 device.status_code
        // TODO: 4. 写入 device_status_log
        log.info("用户 {} 变更设备 {} 状态为 {}，原因：{}",
                UserContext.getUsername(), request.getDeviceId(),
                request.getNewStatus(), request.getReason());

        boolean validTransition = validateTransition(null, request.getNewStatus());
        if (!validTransition) {
            throw new BusinessException(ErrorCode.STATUS_TRANSITION_INVALID);
        }
    }

    /**
     * 校验生命周期状态流转是否合法
     */
    private boolean validateTransition(String oldStatus, String newStatus) {
        // TODO: 实现完整状态机校验
        return oldStatus == null || !oldStatus.equals(newStatus);
    }
}
