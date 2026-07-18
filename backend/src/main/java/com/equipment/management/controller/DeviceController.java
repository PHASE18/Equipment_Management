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
/** 设备基础信息控制器，复用通用 CRUD 流程并提供设备关键词查询。 */
public class DeviceController extends BaseCrudController<DeviceService, Device> {

    /** 注入设备业务服务并交给基础控制器保存。 */
    public DeviceController(DeviceService deviceService) {
        super(deviceService);
    }

    @GetMapping("/list")
    /** 按查询条件返回设备分页结果。 */
    public Result<PageResult<Device>> list(@Valid DeviceQuery query) {
        return Result.success(baseService.page(query));
    }

    @Override
    /** 构造设备列表的关键词过滤和创建时间倒序规则。 */
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
