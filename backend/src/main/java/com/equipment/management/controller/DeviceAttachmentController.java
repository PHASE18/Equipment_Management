package com.equipment.management.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.equipment.management.annotation.RequireAuth;
import com.equipment.management.common.controller.BaseCrudController;
import com.equipment.management.common.query.PageQuery;
import com.equipment.management.entity.DeviceAttachment;
import com.equipment.management.service.DeviceAttachmentService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequireAuth
@RequestMapping("/api/attachment")
public class DeviceAttachmentController extends BaseCrudController<DeviceAttachmentService, DeviceAttachment> {

    public DeviceAttachmentController(DeviceAttachmentService deviceAttachmentService) {
        super(deviceAttachmentService);
    }

    @Override
    protected QueryWrapper<DeviceAttachment> buildQueryWrapper(PageQuery query) {
        QueryWrapper<DeviceAttachment> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.like("file_name", query.getKeyword());
        }
        wrapper.orderByDesc("upload_time");
        return wrapper;
    }
}
