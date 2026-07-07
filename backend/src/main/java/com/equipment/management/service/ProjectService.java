package com.equipment.management.service;

import com.equipment.management.common.result.PageResult;
import com.equipment.management.common.service.BaseCrudService;
import com.equipment.management.dto.request.ProjectQuery;
import com.equipment.management.entity.Project;

public interface ProjectService extends BaseCrudService<Project> {

    PageResult<Project> page(ProjectQuery query);
}
