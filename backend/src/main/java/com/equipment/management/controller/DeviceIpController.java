package com.equipment.management.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.equipment.management.annotation.CrudPermission;
import com.equipment.management.annotation.RequireAuth;
import com.equipment.management.common.controller.BaseCrudController;
import com.equipment.management.common.query.PageQuery;
import com.equipment.management.common.result.Result;
import com.equipment.management.entity.DeviceIp;
import com.equipment.management.service.DeviceIpService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequireAuth
@CrudPermission(module = "device")
@RequestMapping("/api/ip")
/** 设备 IP 地址的查询与维护接口。 */
public class DeviceIpController extends BaseCrudController<DeviceIpService, DeviceIp> {

    public DeviceIpController(DeviceIpService deviceIpService) {
        super(deviceIpService);
    }

    @GetMapping("/list/{deviceId}")
    public Result<List<DeviceIp>> listByDeviceId(@PathVariable Long deviceId) {
        return Result.success(baseService.listByDeviceId(deviceId));
    }

    @GetMapping("/device/{deviceId}")
    public Result<DeviceIp> getByDeviceId(@PathVariable Long deviceId) {
        return Result.success(baseService.getByDeviceId(deviceId));
    }

    @PostMapping("/save")
    public Result<Void> saveByDevice(@Valid @RequestBody DeviceIp entity) {
        baseService.saveByDeviceId(entity);
        return Result.success();
    }

    @Override
    protected QueryWrapper<DeviceIp> buildQueryWrapper(PageQuery query) {
        QueryWrapper<DeviceIp> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("create_time");
        return wrapper;
    }
}
