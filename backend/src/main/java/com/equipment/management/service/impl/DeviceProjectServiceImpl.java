package com.equipment.management.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.equipment.management.common.query.PageQuery;
import com.equipment.management.common.result.PageResult;
import com.equipment.management.common.util.PageUtils;
import com.equipment.management.dto.request.ProjectBindRequest;
import com.equipment.management.entity.DeviceProject;
import com.equipment.management.mapper.DeviceProjectMapper;
import com.equipment.management.service.DeviceProjectService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class DeviceProjectServiceImpl extends ServiceImpl<DeviceProjectMapper, DeviceProject>
        implements DeviceProjectService {

    @Override
    public PageResult<DeviceProject> pageQuery(PageQuery query, Long deviceId, Long projectId) {
        LambdaQueryWrapper<DeviceProject> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(deviceId != null, DeviceProject::getDeviceId, deviceId)
                .eq(projectId != null, DeviceProject::getProjectId, projectId)
                .orderByDesc(DeviceProject::getCreateTime);
        return PageUtils.toPageResult(page(PageUtils.buildPage(query), wrapper));
    }

    @Override
    public void bind(ProjectBindRequest request) {
        long count = count(Wrappers.<DeviceProject>lambdaQuery()
                .eq(DeviceProject::getDeviceId, request.getDeviceId())
                .eq(DeviceProject::getProjectId, request.getProjectId()));
        if (count == 0) {
            DeviceProject deviceProject = new DeviceProject();
            deviceProject.setDeviceId(request.getDeviceId());
            deviceProject.setProjectId(request.getProjectId());
            deviceProject.setCreateTime(LocalDateTime.now());
            save(deviceProject);
        }
    }

    @Override
    public void unbind(ProjectBindRequest request) {
        remove(Wrappers.<DeviceProject>lambdaQuery()
                .eq(DeviceProject::getDeviceId, request.getDeviceId())
                .eq(DeviceProject::getProjectId, request.getProjectId()));
    }
}
