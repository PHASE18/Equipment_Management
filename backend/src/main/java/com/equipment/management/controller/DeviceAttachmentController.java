package com.equipment.management.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.equipment.management.annotation.CrudPermission;
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
@CrudPermission(module = "attachment")
@RequestMapping("/api/attachment")
public class DeviceAttachmentController extends BaseCrudController<DeviceAttachmentService, DeviceAttachment> {

    public DeviceAttachmentController(DeviceAttachmentService deviceAttachmentService) {
        super(deviceAttachmentService);
    }

    @Override
    protected QueryWrapper<DeviceAttachment> buildQueryWrapper(PageQuery query) {
        // 附件列表按上传时间排序，避免 PageQuery 默认 createTime 干扰
        if (!StringUtils.hasText(query.getSortField()) || "createTime".equals(query.getSortField())) {
            query.setSortField("uploadTime");
        }
        QueryWrapper<DeviceAttachment> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.like("file_name", query.getKeyword());
        }
        wrapper.orderByDesc("upload_time");
        return wrapper;
    }
}
