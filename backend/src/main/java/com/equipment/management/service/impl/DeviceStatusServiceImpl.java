package com.equipment.management.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.equipment.management.common.constant.ErrorCode;
import com.equipment.management.common.context.UserContext;
import com.equipment.management.common.enums.DeviceLifecycleStatus;
import com.equipment.management.common.exception.BusinessException;
import com.equipment.management.common.query.PageQuery;
import com.equipment.management.common.result.PageResult;
import com.equipment.management.common.util.PageUtils;
import com.equipment.management.dto.request.DeviceStatusChangeRequest;
import com.equipment.management.dto.response.DeviceStatusChangeResponse;
import com.equipment.management.dto.response.DeviceStatusLogResponse;
import com.equipment.management.entity.Device;
import com.equipment.management.entity.DeviceStatusLog;
import com.equipment.management.entity.SysUser;
import com.equipment.management.mapper.DeviceMapper;
import com.equipment.management.mapper.DeviceStatusLogMapper;
import com.equipment.management.mapper.SysUserMapper;
import com.equipment.management.service.DeviceStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
/** 设备生命周期状态迁移服务实现。 */
public class DeviceStatusServiceImpl implements DeviceStatusService {

    private final DeviceMapper deviceMapper;
    private final DeviceStatusLogMapper deviceStatusLogMapper;
    private final SysUserMapper sysUserMapper;

    @Override
    public PageResult<DeviceStatusLog> listByDeviceId(Long deviceId, PageQuery query) {
        return PageUtils.toPageResult(deviceStatusLogMapper.selectPage(
                PageUtils.buildPage(query),
                Wrappers.<DeviceStatusLog>lambdaQuery()
                        .eq(DeviceStatusLog::getDeviceId, deviceId)
                        .orderByDesc(DeviceStatusLog::getChangeTime)));
    }

    @Override
    public List<DeviceStatusLogResponse> listHistory(Long deviceId) {
        ensureDeviceExists(deviceId);
        List<DeviceStatusLog> logs = deviceStatusLogMapper.selectList(
                Wrappers.<DeviceStatusLog>lambdaQuery()
                        .eq(DeviceStatusLog::getDeviceId, deviceId)
                        .orderByDesc(DeviceStatusLog::getChangeTime));
        return toResponses(logs);
    }

    @Override
    public List<String> allowedNextStatuses(Long deviceId) {
        Device device = getDeviceOrThrow(deviceId);
        return DeviceLifecycleStatus.allowedNextStatuses(device.getStatusCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeviceStatusChangeResponse changeStatus(DeviceStatusChangeRequest request) {
        Device device = getDeviceOrThrow(request.getDeviceId());
        String oldStatus = device.getStatusCode();
        String newStatus = request.resolveTargetStatus();

        if (!StringUtils.hasText(newStatus)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "目标状态不能为空");
        }
        if (StringUtils.hasText(request.getFromStatus()) && !Objects.equals(request.getFromStatus(), oldStatus)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "设备当前状态已变更，请刷新后重试");
        }
        if (Objects.equals(oldStatus, newStatus)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "目标状态与当前状态相同");
        }
        if (!DeviceLifecycleStatus.isValidTransition(oldStatus, newStatus)) {
            throw new BusinessException(ErrorCode.STATUS_TRANSITION_INVALID,
                    "不允许从「" + DeviceLifecycleStatus.labelOf(oldStatus) + "」流转到「"
                            + DeviceLifecycleStatus.labelOf(newStatus) + "」");
        }

        device.setStatusCode(newStatus);
        deviceMapper.updateById(device);

        DeviceStatusLog statusLog = new DeviceStatusLog();
        statusLog.setDeviceId(device.getId());
        statusLog.setOldStatusCode(oldStatus);
        statusLog.setNewStatusCode(newStatus);
        statusLog.setChangeReason(request.getReason());
        statusLog.setRemark(request.getRemark());
        statusLog.setOperatorId(UserContext.getUserId());
        statusLog.setChangeTime(LocalDateTime.now());
        deviceStatusLogMapper.insert(statusLog);

        List<DeviceStatusLogResponse> history = listHistory(device.getId());
        return DeviceStatusChangeResponse.builder()
                .deviceId(device.getId())
                .oldStatusCode(oldStatus)
                .oldStatusName(DeviceLifecycleStatus.labelOf(oldStatus))
                .newStatusCode(newStatus)
                .newStatusName(DeviceLifecycleStatus.labelOf(newStatus))
                .allowedNextStatuses(DeviceLifecycleStatus.allowedNextStatuses(newStatus))
                .history(history)
                .build();
    }

    private Device getDeviceOrThrow(Long deviceId) {
        Device device = deviceMapper.selectById(deviceId);
        if (device == null) {
            throw new BusinessException(ErrorCode.DEVICE_NOT_FOUND);
        }
        return device;
    }

    private void ensureDeviceExists(Long deviceId) {
        getDeviceOrThrow(deviceId);
    }

    private List<DeviceStatusLogResponse> toResponses(List<DeviceStatusLog> logs) {
        Set<Long> operatorIds = logs.stream()
                .map(DeviceStatusLog::getOperatorId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, String> operatorNames = operatorIds.isEmpty()
                ? Map.of()
                : sysUserMapper.selectBatchIds(operatorIds).stream()
                .collect(Collectors.toMap(SysUser::getId,
                        user -> StringUtils.hasText(user.getRealName()) ? user.getRealName() : user.getUsername(),
                        (a, b) -> a));

        return logs.stream()
                .map(log -> DeviceStatusLogResponse.builder()
                        .id(log.getId())
                        .deviceId(log.getDeviceId())
                        .oldStatusCode(log.getOldStatusCode())
                        .oldStatusName(DeviceLifecycleStatus.labelOf(log.getOldStatusCode()))
                        .newStatusCode(log.getNewStatusCode())
                        .newStatusName(DeviceLifecycleStatus.labelOf(log.getNewStatusCode()))
                        .changeReason(log.getChangeReason())
                        .remark(log.getRemark())
                        .operatorId(log.getOperatorId())
                        .operatorName(log.getOperatorId() != null ? operatorNames.get(log.getOperatorId()) : null)
                        .changeTime(log.getChangeTime())
                        .build())
                .toList();
    }
}
