package com.equipment.management.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.equipment.management.common.constant.DictTypeConstants;
import com.equipment.management.common.enums.DeviceLifecycleStatus;
import com.equipment.management.common.result.PageResult;
import com.equipment.management.common.util.ExcelUtils;
import com.equipment.management.dto.request.DeviceQuery;
import com.equipment.management.dto.response.DeviceExcelRow;
import com.equipment.management.dto.response.ExcelImportResponse;
import com.equipment.management.entity.Device;
import com.equipment.management.entity.DeviceConfig;
import com.equipment.management.entity.DeviceIp;
import com.equipment.management.entity.DeviceProject;
import com.equipment.management.entity.Project;
import com.equipment.management.entity.SysDepartment;
import com.equipment.management.entity.SysDict;
import com.equipment.management.entity.SysUser;
import com.equipment.management.service.DeviceConfigService;
import com.equipment.management.service.DeviceIpService;
import com.equipment.management.service.DeviceProjectService;
import com.equipment.management.service.DeviceService;
import com.equipment.management.service.DictService;
import com.equipment.management.service.ExcelService;
import com.equipment.management.service.ProjectService;
import com.equipment.management.service.SysDepartmentService;
import com.equipment.management.service.SysUserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
/** Excel 导入导出服务实现。 */
public class ExcelServiceImpl implements ExcelService {

    private static final long EXPORT_MAX_SIZE = 5000L;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final DeviceService deviceService;
    private final DeviceConfigService deviceConfigService;
    private final DeviceIpService deviceIpService;
    private final DeviceProjectService deviceProjectService;
    private final ProjectService projectService;
    private final SysDepartmentService departmentService;
    private final SysUserService userService;
    private final DictService dictService;

    @Override
    public ExcelImportResponse importDevices(MultipartFile file) {
        ExcelUtils.validateExcelFile(file);
        // TODO: EasyExcel 解析并批量导入，校验 SN/设备编号唯一性
        return ExcelImportResponse.builder()
                .successCount(0)
                .failCount(0)
                .message("导入完成")
                .build();
    }

    @Override
    public void exportDevices(DeviceQuery query, HttpServletResponse response) {
        if (query == null) {
            query = new DeviceQuery();
        }
        query.setPageNum(1);
        query.setPageSize(EXPORT_MAX_SIZE);
        PageResult<Device> page = deviceService.page(query);
        List<Device> devices = page.getRecords() == null ? List.of() : page.getRecords();
        List<DeviceExcelRow> rows = toExcelRows(devices);
        ExcelUtils.write(response, "devices.xlsx", "设备档案", DeviceExcelRow.class, rows);
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) {
        ExcelUtils.write(response, "device_import_template.xlsx", "设备档案",
                DeviceExcelRow.class, List.of());
    }

    private List<DeviceExcelRow> toExcelRows(List<Device> devices) {
        if (CollectionUtils.isEmpty(devices)) {
            return List.of();
        }
        List<Long> deviceIds = devices.stream().map(Device::getId).filter(Objects::nonNull).toList();

        Map<Long, DeviceConfig> configMap = loadConfigs(deviceIds);
        Map<Long, DeviceIp> ipMap = loadIps(deviceIds);
        Map<Long, String> projectNamesMap = loadProjectNames(deviceIds);
        Map<Long, String> deptNames = loadDepartmentNames();
        Map<Long, String> userNames = loadUserNames(devices);
        Map<String, String> brandNames = loadDictNames(DictTypeConstants.DEVICE_BRAND);
        Map<String, String> typeNames = loadDictNames(DictTypeConstants.DEVICE_TYPE);

        return devices.stream()
                .map(device -> toExcelRow(device, configMap.get(device.getId()), ipMap.get(device.getId()),
                        projectNamesMap.getOrDefault(device.getId(), ""),
                        deptNames, userNames, brandNames, typeNames))
                .toList();
    }

    private DeviceExcelRow toExcelRow(Device device, DeviceConfig config, DeviceIp ip, String projectNames,
                                      Map<Long, String> deptNames, Map<Long, String> userNames,
                                      Map<String, String> brandNames, Map<String, String> typeNames) {
        DeviceExcelRow row = new DeviceExcelRow();
        row.setDeviceNo(device.getDeviceNo());
        row.setDeviceName(device.getDeviceName());
        row.setSn(device.getSn());
        row.setAssetNo(device.getAssetNo());
        row.setIsFixedAsset(Integer.valueOf(1).equals(device.getIsFixedAsset()) ? "是" : "否");
        row.setBrandName(resolveDictName(brandNames, device.getBrandCode()));
        row.setModel(device.getModel());
        row.setDeviceTypeName(resolveDictName(typeNames, device.getDeviceTypeCode()));
        row.setStatusName(DeviceLifecycleStatus.labelOf(device.getStatusCode()));
        row.setDepartmentName(deptNames.getOrDefault(device.getDepartmentId(), ""));
        row.setUseDepartmentName(deptNames.getOrDefault(device.getUseDepartmentId(), ""));
        row.setManagerName(StringUtils.hasText(device.getManagerName())
                ? device.getManagerName()
                : userNames.getOrDefault(device.getManagerUserId(), ""));
        row.setUseUserName(device.getUseUserName());
        row.setOriginalValue(formatDecimal(device.getOriginalValue()));
        row.setApprovalNo(device.getApprovalNo());
        row.setSupplier(device.getSupplier());
        row.setMaintenanceCompany(device.getMaintenanceCompany());
        row.setPurchaseDate(formatDate(device.getPurchaseDate()));
        row.setManufactureDate(formatDate(device.getManufactureDate()));
        row.setOnlineDate(formatDate(device.getOnlineDate()));
        row.setWarrantyEnd(formatDate(device.getWarrantyEnd()));
        row.setScrapDate(formatDate(device.getScrapDate()));
        row.setLocation(device.getLocation());
        row.setCabinet(device.getCabinet());
        row.setProjectNames(projectNames);
        row.setRemark(device.getRemark());

        if (config != null) {
            row.setCpu(config.getCpu());
            row.setMemory(config.getMemory());
            row.setDisk(config.getDisk());
            row.setRaid(config.getRaid());
            row.setGpu(config.getGpu());
            row.setFiberCard(config.getFiberCard());
            row.setNic(config.getNic());
            row.setPowerSupply(config.getPowerSupply());
            row.setOs(config.getOs());
            row.setDbVersion(config.getDbVersion());
            row.setFirmware(config.getFirmware());
            row.setBios(config.getBios());
            row.setConfigRemark(config.getRemark());
        }
        if (ip != null) {
            row.setBusinessIp(ip.getBusinessIp());
            row.setManagementIp(ip.getManagementIp());
            row.setMask(ip.getMask());
            row.setGateway(ip.getGateway());
            row.setMountedBusiness(ip.getMountedBusiness());
            row.setNetworkZone(ip.getNetworkZone());
            row.setMgmtLoginMethod(ip.getMgmtLoginMethod());
        }
        return row;
    }

    private Map<Long, DeviceConfig> loadConfigs(List<Long> deviceIds) {
        if (deviceIds.isEmpty()) {
            return Map.of();
        }
        return deviceConfigService.list(Wrappers.<DeviceConfig>lambdaQuery()
                        .in(DeviceConfig::getDeviceId, deviceIds))
                .stream()
                .collect(Collectors.toMap(DeviceConfig::getDeviceId, item -> item, (a, b) -> a));
    }

    private Map<Long, DeviceIp> loadIps(List<Long> deviceIds) {
        if (deviceIds.isEmpty()) {
            return Map.of();
        }
        return deviceIpService.list(Wrappers.<DeviceIp>lambdaQuery()
                        .in(DeviceIp::getDeviceId, deviceIds))
                .stream()
                .collect(Collectors.toMap(DeviceIp::getDeviceId, item -> item, (a, b) -> a));
    }

    private Map<Long, String> loadProjectNames(List<Long> deviceIds) {
        if (deviceIds.isEmpty()) {
            return Map.of();
        }
        List<DeviceProject> relations = deviceProjectService.list(Wrappers.<DeviceProject>lambdaQuery()
                .in(DeviceProject::getDeviceId, deviceIds));
        if (relations.isEmpty()) {
            return Map.of();
        }
        Set<Long> projectIds = relations.stream()
                .map(DeviceProject::getProjectId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, String> projectNameById = projectIds.isEmpty()
                ? Map.of()
                : projectService.listByIds(projectIds).stream()
                .collect(Collectors.toMap(Project::getId, Project::getProjectName, (a, b) -> a));

        Map<Long, String> result = new HashMap<>();
        Map<Long, List<DeviceProject>> byDevice = relations.stream()
                .collect(Collectors.groupingBy(DeviceProject::getDeviceId));
        byDevice.forEach((deviceId, list) -> {
            String names = list.stream()
                    .map(DeviceProject::getProjectId)
                    .map(projectNameById::get)
                    .filter(StringUtils::hasText)
                    .distinct()
                    .collect(Collectors.joining("、"));
            result.put(deviceId, names);
        });
        return result;
    }

    private Map<Long, String> loadDepartmentNames() {
        return departmentService.list().stream()
                .filter(item -> item.getId() != null)
                .collect(Collectors.toMap(SysDepartment::getId, SysDepartment::getDepartmentName, (a, b) -> a));
    }

    private Map<Long, String> loadUserNames(List<Device> devices) {
        Set<Long> userIds = devices.stream()
                .map(Device::getManagerUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (userIds.isEmpty()) {
            return Map.of();
        }
        return userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(SysUser::getId, this::formatUserName, (a, b) -> a));
    }

    private Map<String, String> loadDictNames(String dictType) {
        List<SysDict> dicts = dictService.listByType(dictType);
        if (dicts == null) {
            return Collections.emptyMap();
        }
        return dicts.stream()
                .filter(item -> StringUtils.hasText(item.getDictCode()))
                .collect(Collectors.toMap(SysDict::getDictCode, SysDict::getDictName, (a, b) -> a));
    }

    private String resolveDictName(Map<String, String> map, String code) {
        if (!StringUtils.hasText(code)) {
            return "";
        }
        return map.getOrDefault(code, code);
    }

    private String formatUserName(SysUser user) {
        if (StringUtils.hasText(user.getRealName())) {
            return user.getRealName() + "（" + user.getUsername() + "）";
        }
        return user.getUsername();
    }

    private String formatDate(LocalDate date) {
        return date == null ? "" : DATE_FMT.format(date);
    }

    private String formatDecimal(BigDecimal value) {
        return value == null ? "" : value.stripTrailingZeros().toPlainString();
    }
}
