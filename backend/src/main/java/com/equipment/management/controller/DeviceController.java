package com.equipment.management.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.equipment.management.annotation.CrudPermission;
import com.equipment.management.annotation.RequireAuth;
import com.equipment.management.common.controller.BaseCrudController;
import com.equipment.management.common.query.PageQuery;
import com.equipment.management.common.result.PageResult;
import com.equipment.management.common.result.Result;
import com.equipment.management.dto.request.DeviceQuery;
import com.equipment.management.entity.Device;
import com.equipment.management.service.DeviceService;
import jakarta.validation.Valid;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequireAuth
@CrudPermission(module = "device")
@RequestMapping("/api/device")
public class DeviceController extends BaseCrudController<DeviceService, Device> {

    public DeviceController(DeviceService deviceService) {
        super(deviceService);
    }

    @GetMapping("/list")
    public Result<PageResult<Device>> list(@Valid DeviceQuery query) {
        return Result.success(baseService.page(query));
    }

    @Override
    protected QueryWrapper<Device> buildQueryWrapper(PageQuery query) {
        QueryWrapper<Device> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like("device_no", query.getKeyword())
                    .or().like("device_name", query.getKeyword())
                    .or().like("sn", query.getKeyword()));
        }
        wrapper.orderByDesc("create_time");
        return wrapper;
    }
}
