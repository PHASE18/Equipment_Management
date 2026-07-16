package com.equipment.management.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.equipment.management.common.constant.ErrorCode;
import com.equipment.management.common.exception.BusinessException;
import com.equipment.management.entity.Project;
import com.equipment.management.mapper.ProjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceImplTest {

    @Mock
    private ProjectMapper projectMapper;

    private ProjectServiceImpl projectService;

    @BeforeEach
    void setUp() {
        projectService = new ProjectServiceImpl();
        ReflectionTestUtils.setField(projectService, "baseMapper", projectMapper);
    }

    @Test
    @DisplayName("新增项目：编码不重复时应保存成功")
    void createEntity_whenCodeUnique_shouldSave() {
        Project project = buildProject(null, "PRJ-001", "测试项目");
        when(projectMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(projectMapper.insert(any(Project.class))).thenReturn(1);

        projectService.createEntity(project);

        ArgumentCaptor<Project> captor = ArgumentCaptor.forClass(Project.class);
        verify(projectMapper).insert(captor.capture());
        assertEquals("PRJ-001", captor.getValue().getProjectCode());
        assertEquals("测试项目", captor.getValue().getProjectName());
    }

    @Test
    @DisplayName("新增项目：编码重复时应抛出 CONFLICT")
    void createEntity_whenCodeDuplicate_shouldThrowConflict() {
        Project project = buildProject(null, "PRJ-DUP", "重复项目");
        when(projectMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> projectService.createEntity(project));

        assertEquals(ErrorCode.CONFLICT.getCode(), ex.getCode());
        verify(projectMapper, never()).insert(any());
    }

    @Test
    @DisplayName("修改项目：编码不冲突时应更新成功")
    void updateEntity_whenCodeUnique_shouldUpdate() {
        Project existing = buildProject(10L, "PRJ-010", "原项目");
        Project update = buildProject(10L, "PRJ-010", "更新后项目");
        when(projectMapper.selectById(10L)).thenReturn(existing);
        when(projectMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(projectMapper.updateById(any(Project.class))).thenReturn(1);

        projectService.updateEntity(update);

        verify(projectMapper).updateById(any(Project.class));
    }

    @Test
    @DisplayName("修改项目：改成已存在编码时应抛出 CONFLICT")
    void updateEntity_whenCodeDuplicate_shouldThrowConflict() {
        Project update = buildProject(10L, "PRJ-OTHER", "项目");
        when(projectMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> projectService.updateEntity(update));

        assertEquals(ErrorCode.CONFLICT.getCode(), ex.getCode());
        verify(projectMapper, never()).updateById(any());
    }

    @Test
    @DisplayName("修改项目：ID不存在时应抛出 NOT_FOUND")
    void updateEntity_whenNotFound_shouldThrowNotFound() {
        Project update = buildProject(999L, "PRJ-999", "不存在");
        when(projectMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(projectMapper.selectById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> projectService.updateEntity(update));

        assertEquals(ErrorCode.NOT_FOUND.getCode(), ex.getCode());
    }

    private Project buildProject(Long id, String code, String name) {
        Project project = new Project();
        project.setId(id);
        project.setProjectCode(code);
        project.setProjectName(name);
        project.setDepartmentId(1L);
        return project;
    }
}
