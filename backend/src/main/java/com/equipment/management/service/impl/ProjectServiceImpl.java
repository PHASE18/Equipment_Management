package com.equipment.management.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.equipment.management.common.constant.ErrorCode;
import com.equipment.management.common.exception.BusinessException;
import com.equipment.management.common.result.PageResult;
import com.equipment.management.dto.request.ProjectQuery;
import com.equipment.management.entity.Project;
import com.equipment.management.mapper.ProjectMapper;
import com.equipment.management.common.service.impl.BaseCrudServiceImpl;
import com.equipment.management.service.ProjectService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
/** 项目基础信息服务实现。 */
public class ProjectServiceImpl extends BaseCrudServiceImpl<ProjectMapper, Project> implements ProjectService {

    @Override
    public PageResult<Project> page(ProjectQuery query) {
        LambdaQueryWrapper<Project> wrapper = Wrappers.lambdaQuery();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(Project::getProjectName, query.getKeyword())
                    .or().like(Project::getProjectCode, query.getKeyword()));
        }
        return pageQuery(query, wrapper);
    }

    @Override
    public void createEntity(Project entity) {
        validateProjectCode(entity, null);
        super.createEntity(entity);
    }

    @Override
    public void updateEntity(Project entity) {
        validateProjectCode(entity, entity.getId());
        super.updateEntity(entity);
    }

    private void validateProjectCode(Project entity, Long excludeId) {
        if (!StringUtils.hasText(entity.getProjectCode())) {
            return;
        }
        long count = count(Wrappers.<Project>lambdaQuery()
                .eq(Project::getProjectCode, entity.getProjectCode())
                .ne(excludeId != null, Project::getId, excludeId));
        if (count > 0) {
            throw new BusinessException(ErrorCode.CONFLICT);
        }
    }
}
