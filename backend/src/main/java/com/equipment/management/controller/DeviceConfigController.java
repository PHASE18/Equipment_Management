package com.equipment.management.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.equipment.management.annotation.RequireAuth;
import com.equipment.management.common.controller.BaseCrudController;
import com.equipment.management.common.query.PageQuery;
import com.equipment.management.entity.DeviceConfig;
import com.equipment.management.service.DeviceConfigService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequireAuth
@RequestMapping("/api/device-config")
public class DeviceConfigController extends BaseCrudController<DeviceConfigService, DeviceConfig> {

    public DeviceConfigController(DeviceConfigService deviceConfigService) {
        super(deviceConfigService);
    }

    @Override
    protected QueryWrapper<DeviceConfig> buildQueryWrapper(PageQuery query) {
        QueryWrapper<DeviceConfig> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("create_time");
        return wrapper;
    }
}
