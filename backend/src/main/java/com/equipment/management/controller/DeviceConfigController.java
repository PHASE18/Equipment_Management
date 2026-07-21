package com.equipment.management.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.equipment.management.annotation.CrudPermission;
import com.equipment.management.annotation.RequireAuth;
import com.equipment.management.common.controller.BaseCrudController;
import com.equipment.management.common.query.PageQuery;
import com.equipment.management.common.result.Result;
import com.equipment.management.entity.DeviceConfig;
import com.equipment.management.service.DeviceConfigService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequireAuth
@CrudPermission(module = "device")
@RequestMapping("/api/device-config")
/** 设备扩展配置的查询与维护接口。 */
public class DeviceConfigController extends BaseCrudController<DeviceConfigService, DeviceConfig> {

    public DeviceConfigController(DeviceConfigService deviceConfigService) {
        super(deviceConfigService);
    }

    @GetMapping("/device/{deviceId}")
    public Result<DeviceConfig> getByDeviceId(@PathVariable Long deviceId) {
        return Result.success(baseService.getByDeviceId(deviceId));
    }

    @PostMapping("/save")
    public Result<Void> saveByDevice(@Valid @RequestBody DeviceConfig entity) {
        baseService.saveByDeviceId(entity);
        return Result.success();
    }

    @Override
    protected QueryWrapper<DeviceConfig> buildQueryWrapper(PageQuery query) {
        QueryWrapper<DeviceConfig> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("create_time");
        return wrapper;
    }
}
