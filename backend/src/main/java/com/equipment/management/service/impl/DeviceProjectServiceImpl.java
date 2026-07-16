package com.equipment.management.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.equipment.management.common.query.PageQuery;
import com.equipment.management.common.result.PageResult;
import com.equipment.management.common.util.PageUtils;
import com.equipment.management.dto.request.DeviceProjectSyncRequest;
import com.equipment.management.dto.request.ProjectBindRequest;
import com.equipment.management.entity.DeviceProject;
import com.equipment.management.mapper.DeviceProjectMapper;
import com.equipment.management.service.DeviceProjectService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

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
    public List<Long> listProjectIdsByDeviceId(Long deviceId) {
        if (deviceId == null) {
            return Collections.emptyList();
        }
        return list(Wrappers.<DeviceProject>lambdaQuery().eq(DeviceProject::getDeviceId, deviceId))
                .stream()
                .map(DeviceProject::getProjectId)
                .toList();
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncProjects(DeviceProjectSyncRequest request) {
        List<Long> currentIds = listProjectIdsByDeviceId(request.getDeviceId());
        List<Long> nextIds = request.getProjectIds() == null ? Collections.emptyList() : request.getProjectIds();

        for (Long projectId : nextIds) {
            if (!currentIds.contains(projectId)) {
                ProjectBindRequest bindRequest = new ProjectBindRequest();
                bindRequest.setDeviceId(request.getDeviceId());
                bindRequest.setProjectId(projectId);
                bind(bindRequest);
            }
        }
        for (Long projectId : currentIds) {
            if (!nextIds.contains(projectId)) {
                ProjectBindRequest unbindRequest = new ProjectBindRequest();
                unbindRequest.setDeviceId(request.getDeviceId());
                unbindRequest.setProjectId(projectId);
                unbind(unbindRequest);
            }
        }
    }
}
