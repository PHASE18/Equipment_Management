package com.equipment.management.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.equipment.management.common.constant.ErrorCode;
import com.equipment.management.common.exception.BusinessException;
import com.equipment.management.common.result.PageResult;
import com.equipment.management.common.util.PageUtils;
import com.equipment.management.dto.request.DeviceStatusChangeRequest;
import com.equipment.management.dto.request.MaintenanceQuery;
import com.equipment.management.dto.response.FaultTypeStatResponse;
import com.equipment.management.dto.response.FileUploadResponse;
import com.equipment.management.dto.response.MaintenanceDetailResponse;
import com.equipment.management.entity.Device;
import com.equipment.management.entity.DeviceMaintenance;
import com.equipment.management.entity.SysDict;
import com.equipment.management.mapper.DeviceMaintenanceMapper;
import com.equipment.management.mapper.DeviceMapper;
import com.equipment.management.common.service.impl.BaseCrudServiceImpl;
import com.equipment.management.service.DeviceMaintenanceService;
import com.equipment.management.service.DeviceStatusService;
import com.equipment.management.service.DictService;
import com.equipment.management.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeviceMaintenanceServiceImpl extends BaseCrudServiceImpl<DeviceMaintenanceMapper, DeviceMaintenance>
        implements DeviceMaintenanceService {

    private static final String STATUS_IN_USE = "IN_USE";
    private static final String STATUS_MAINTAINING = "MAINTAINING";

    private final DeviceMapper deviceMapper;
    private final DeviceStatusService deviceStatusService;
    private final DictService dictService;
    private final FileService fileService;

    @Override
    public PageResult<MaintenanceDetailResponse> page(MaintenanceQuery query) {
        LambdaQueryWrapper<DeviceMaintenance> wrapper = buildWrapper(query);
        Page<DeviceMaintenance> page = page(PageUtils.buildPage(query), wrapper);
        List<MaintenanceDetailResponse> records = toDetailList(page.getRecords());
        return PageResult.of(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public MaintenanceDetailResponse getDetailVo(Long id) {
        DeviceMaintenance maintenance = getDetail(id);
        return toDetail(maintenance);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createMaintenance(DeviceMaintenance entity) {
        validateMaintenance(entity);
        ensureDeviceExists(entity.getDeviceId());
        if (entity.getIsResolved() == null) {
            entity.setIsResolved(0);
        }
        save(entity);
        syncAttachmentPath(entity.getId());
        transitionDeviceToMaintaining(entity.getDeviceId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMaintenance(DeviceMaintenance entity) {
        DeviceMaintenance existing = getDetail(entity.getId());
        validateMaintenance(entity);
        ensureDeviceExists(entity.getDeviceId());
        updateById(entity);
        syncAttachmentPath(entity.getId());
        if (Objects.equals(existing.getIsResolved(), 0) && Objects.equals(entity.getIsResolved(), 1)) {
            recoverDevice(entity.getDeviceId(), entity.getRecoverDate());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeMaintenance(Long id) {
        DeviceMaintenance maintenance = getDetail(id);
        if (Objects.equals(maintenance.getIsResolved(), 1)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "维修工单已完成");
        }
        maintenance.setIsResolved(1);
        if (maintenance.getRecoverDate() == null) {
            maintenance.setRecoverDate(LocalDate.now());
        }
        updateById(maintenance);
        recoverDevice(maintenance.getDeviceId(), maintenance.getRecoverDate());
    }

    @Override
    public List<FaultTypeStatResponse> faultTypeStatistics() {
        List<DeviceMaintenance> records = list(Wrappers.<DeviceMaintenance>lambdaQuery()
                .isNotNull(DeviceMaintenance::getFaultTypeCode)
                .ne(DeviceMaintenance::getFaultTypeCode, ""));
        if (records.isEmpty()) {
            return Collections.emptyList();
        }
        Map<String, Long> grouped = records.stream()
                .collect(Collectors.groupingBy(DeviceMaintenance::getFaultTypeCode, Collectors.counting()));
        Map<String, String> faultTypeNames = dictService.listByType("fault_type").stream()
                .collect(Collectors.toMap(SysDict::getDictCode, SysDict::getDictName, (a, b) -> a));
        return grouped.entrySet().stream()
                .map(entry -> FaultTypeStatResponse.builder()
                        .faultTypeCode(entry.getKey())
                        .faultTypeName(faultTypeNames.getOrDefault(entry.getKey(), entry.getKey()))
                        .count(entry.getValue())
                        .build())
                .sorted((a, b) -> Long.compare(b.getCount(), a.getCount()))
                .toList();
    }

    @Override
    public void createEntity(DeviceMaintenance entity) {
        createMaintenance(entity);
    }

    @Override
    public void updateEntity(DeviceMaintenance entity) {
        updateMaintenance(entity);
    }

    private LambdaQueryWrapper<DeviceMaintenance> buildWrapper(MaintenanceQuery query) {
        LambdaQueryWrapper<DeviceMaintenance> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(query.getDeviceId() != null, DeviceMaintenance::getDeviceId, query.getDeviceId())
                .eq(StringUtils.hasText(query.getFaultType()), DeviceMaintenance::getFaultTypeCode, query.getFaultType())
                .eq(query.getIsResolved() != null, DeviceMaintenance::getIsResolved, query.getIsResolved());
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(DeviceMaintenance::getMaintenancePerson, query.getKeyword())
                    .or().like(DeviceMaintenance::getFaultDescription, query.getKeyword())
                    .or().like(DeviceMaintenance::getMaintenanceCompany, query.getKeyword()));
        }
        wrapper.orderByDesc(DeviceMaintenance::getMaintenanceDate)
                .orderByDesc(DeviceMaintenance::getCreateTime);
        return wrapper;
    }

    private void validateMaintenance(DeviceMaintenance entity) {
        if (entity.getDeviceId() == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请绑定设备");
        }
        if (entity.getMaintenanceDate() == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "维修日期不能为空");
        }
    }

    private void ensureDeviceExists(Long deviceId) {
        if (deviceMapper.selectById(deviceId) == null) {
            throw new BusinessException(ErrorCode.DEVICE_NOT_FOUND);
        }
    }

    private void transitionDeviceToMaintaining(Long deviceId) {
        Device device = deviceMapper.selectById(deviceId);
        if (device == null || !STATUS_IN_USE.equals(device.getStatusCode())) {
            return;
        }
        DeviceStatusChangeRequest request = new DeviceStatusChangeRequest();
        request.setDeviceId(deviceId);
        request.setFromStatus(device.getStatusCode());
        request.setToStatus(STATUS_MAINTAINING);
        request.setReason("新增维修工单，设备进入维修中");
        request.setRemark("系统自动流转");
        deviceStatusService.changeStatus(request);
    }

    private void recoverDevice(Long deviceId, LocalDate recoverDate) {
        Device device = deviceMapper.selectById(deviceId);
        if (device == null || !STATUS_MAINTAINING.equals(device.getStatusCode())) {
            return;
        }
        DeviceStatusChangeRequest request = new DeviceStatusChangeRequest();
        request.setDeviceId(deviceId);
        request.setFromStatus(device.getStatusCode());
        request.setToStatus(STATUS_IN_USE);
        request.setReason("维修完成恢复使用");
        request.setRemark(recoverDate != null ? "恢复日期：" + recoverDate : "系统自动流转");
        deviceStatusService.changeStatus(request);
    }

    private void syncAttachmentPath(Long maintenanceId) {
        List<FileUploadResponse> attachments = fileService.listByMaintenanceId(maintenanceId);
        if (attachments.isEmpty()) {
            return;
        }
        DeviceMaintenance update = new DeviceMaintenance();
        update.setId(maintenanceId);
        update.setAttachmentPath(attachments.get(0).getFilePath());
        updateById(update);
    }

    private List<MaintenanceDetailResponse> toDetailList(List<DeviceMaintenance> records) {
        if (records.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Long> deviceIds = records.stream().map(DeviceMaintenance::getDeviceId).collect(Collectors.toSet());
        Map<Long, Device> deviceMap = deviceMapper.selectBatchIds(deviceIds).stream()
                .collect(Collectors.toMap(Device::getId, item -> item, (a, b) -> a));
        Map<String, String> faultTypeNames = dictService.listByType("fault_type").stream()
                .collect(Collectors.toMap(SysDict::getDictCode, SysDict::getDictName, (a, b) -> a));

        return records.stream()
                .map(item -> buildDetail(item, deviceMap.get(item.getDeviceId()), faultTypeNames, Collections.emptyList()))
                .toList();
    }

    private MaintenanceDetailResponse toDetail(DeviceMaintenance maintenance) {
        Device device = deviceMapper.selectById(maintenance.getDeviceId());
        Map<String, String> faultTypeNames = dictService.listByType("fault_type").stream()
                .collect(Collectors.toMap(SysDict::getDictCode, SysDict::getDictName, (a, b) -> a));
        List<FileUploadResponse> attachments = fileService.listByMaintenanceId(maintenance.getId());
        return buildDetail(maintenance, device, faultTypeNames, attachments);
    }

    private MaintenanceDetailResponse buildDetail(DeviceMaintenance maintenance, Device device,
                                                  Map<String, String> faultTypeNames,
                                                  List<FileUploadResponse> attachments) {
        return MaintenanceDetailResponse.builder()
                .id(maintenance.getId())
                .deviceId(maintenance.getDeviceId())
                .deviceNo(device != null ? device.getDeviceNo() : null)
                .deviceName(device != null ? device.getDeviceName() : null)
                .maintenanceDate(maintenance.getMaintenanceDate())
                .maintenancePerson(maintenance.getMaintenancePerson())
                .maintenanceCompany(maintenance.getMaintenanceCompany())
                .faultTypeCode(maintenance.getFaultTypeCode())
                .faultTypeName(faultTypeNames.getOrDefault(maintenance.getFaultTypeCode(), maintenance.getFaultTypeCode()))
                .faultReason(maintenance.getFaultReason())
                .faultDescription(maintenance.getFaultDescription())
                .replaceParts(maintenance.getReplaceParts())
                .maintenanceCost(maintenance.getMaintenanceCost())
                .recoverDate(maintenance.getRecoverDate())
                .isResolved(maintenance.getIsResolved())
                .attachmentPath(maintenance.getAttachmentPath())
                .remark(maintenance.getRemark())
                .createTime(maintenance.getCreateTime())
                .attachments(attachments)
                .build();
    }
}
