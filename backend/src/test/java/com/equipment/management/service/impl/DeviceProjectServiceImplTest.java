package com.equipment.management.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.equipment.management.dto.request.DeviceProjectSyncRequest;
import com.equipment.management.dto.request.ProjectBindRequest;
import com.equipment.management.entity.DeviceProject;
import com.equipment.management.mapper.DeviceProjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeviceProjectServiceImplTest {

    @Mock
    private DeviceProjectMapper deviceProjectMapper;

    private DeviceProjectServiceImpl deviceProjectService;

    @BeforeEach
    void setUp() {
        deviceProjectService = new DeviceProjectServiceImpl();
        ReflectionTestUtils.setField(deviceProjectService, "baseMapper", deviceProjectMapper);
    }

    @Test
    @DisplayName("绑定：未关联时应新增关联记录")
    void bind_whenNotExists_shouldInsert() {
        ProjectBindRequest request = bindRequest(1L, 10L);
        when(deviceProjectMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(deviceProjectMapper.insert(any(DeviceProject.class))).thenReturn(1);

        deviceProjectService.bind(request);

        ArgumentCaptor<DeviceProject> captor = ArgumentCaptor.forClass(DeviceProject.class);
        verify(deviceProjectMapper).insert(captor.capture());
        assertEquals(1L, captor.getValue().getDeviceId());
        assertEquals(10L, captor.getValue().getProjectId());
    }

    @Test
    @DisplayName("绑定：已关联时应幂等跳过")
    void bind_whenAlreadyExists_shouldSkip() {
        ProjectBindRequest request = bindRequest(1L, 10L);
        when(deviceProjectMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        deviceProjectService.bind(request);

        verify(deviceProjectMapper, never()).insert(any());
    }

    @Test
    @DisplayName("解绑：应按设备与项目删除关联")
    void unbind_shouldRemoveRelation() {
        ProjectBindRequest request = bindRequest(1L, 10L);
        when(deviceProjectMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(1);

        deviceProjectService.unbind(request);

        verify(deviceProjectMapper).delete(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("查询设备项目：deviceId为空应返回空列表")
    void listProjectIdsByDeviceId_whenNull_shouldReturnEmpty() {
        List<Long> result = deviceProjectService.listProjectIdsByDeviceId(null);
        assertTrue(result.isEmpty());
        verify(deviceProjectMapper, never()).selectList(any());
    }

    @Test
    @DisplayName("查询设备项目：应返回项目ID列表")
    void listProjectIdsByDeviceId_shouldReturnIds() {
        DeviceProject rel1 = relation(1L, 10L);
        DeviceProject rel2 = relation(1L, 20L);
        when(deviceProjectMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(rel1, rel2));

        List<Long> result = deviceProjectService.listProjectIdsByDeviceId(1L);

        assertEquals(List.of(10L, 20L), result);
    }

    @Test
    @DisplayName("同步：应新增缺失关联并删除多余关联")
    void syncProjects_shouldAddMissingAndRemoveExtra() {
        DeviceProject existing = relation(1L, 10L);
        when(deviceProjectMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(existing))
                .thenReturn(Collections.emptyList());
        when(deviceProjectMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(deviceProjectMapper.insert(any(DeviceProject.class))).thenReturn(1);
        when(deviceProjectMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(1);

        DeviceProjectSyncRequest request = new DeviceProjectSyncRequest();
        request.setDeviceId(1L);
        request.setProjectIds(List.of(20L));

        deviceProjectService.syncProjects(request);

        verify(deviceProjectMapper, times(1)).insert(any(DeviceProject.class));
        verify(deviceProjectMapper, times(1)).delete(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("同步：projectIds为空时应清空全部关联")
    void syncProjects_whenEmptyIds_shouldUnbindAll() {
        DeviceProject existing = relation(1L, 10L);
        when(deviceProjectMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(existing));
        when(deviceProjectMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(1);

        DeviceProjectSyncRequest request = new DeviceProjectSyncRequest();
        request.setDeviceId(1L);
        request.setProjectIds(Collections.emptyList());

        deviceProjectService.syncProjects(request);

        verify(deviceProjectMapper, never()).insert(any());
        verify(deviceProjectMapper).delete(any(LambdaQueryWrapper.class));
    }

    private ProjectBindRequest bindRequest(Long deviceId, Long projectId) {
        ProjectBindRequest request = new ProjectBindRequest();
        request.setDeviceId(deviceId);
        request.setProjectId(projectId);
        return request;
    }

    private DeviceProject relation(Long deviceId, Long projectId) {
        DeviceProject relation = new DeviceProject();
        relation.setDeviceId(deviceId);
        relation.setProjectId(projectId);
        return relation;
    }
}
