package com.equipment.management.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.equipment.management.common.query.PageQuery;
import com.equipment.management.common.result.PageResult;
import com.equipment.management.dto.request.ProjectBindRequest;
import com.equipment.management.entity.DeviceProject;

public interface DeviceProjectService extends IService<DeviceProject> {

    PageResult<DeviceProject> pageQuery(PageQuery query, Long deviceId, Long projectId);

    void bind(ProjectBindRequest request);

    void unbind(ProjectBindRequest request);
}
