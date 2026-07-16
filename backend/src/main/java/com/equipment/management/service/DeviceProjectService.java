package com.equipment.management.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.equipment.management.common.query.PageQuery;
import com.equipment.management.common.result.PageResult;
import com.equipment.management.dto.request.DeviceProjectSyncRequest;
import com.equipment.management.dto.request.ProjectBindRequest;
import com.equipment.management.entity.DeviceProject;

import java.util.List;

public interface DeviceProjectService extends IService<DeviceProject> {

    PageResult<DeviceProject> pageQuery(PageQuery query, Long deviceId, Long projectId);

    List<Long> listProjectIdsByDeviceId(Long deviceId);

    void bind(ProjectBindRequest request);

    void unbind(ProjectBindRequest request);

    void syncProjects(DeviceProjectSyncRequest request);
}
