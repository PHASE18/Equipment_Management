package com.equipment.management.service;

import com.equipment.management.common.service.BaseCrudService;
import com.equipment.management.entity.SysDepartment;

import java.util.List;

/** 部门树管理领域服务。 */
public interface SysDepartmentService extends BaseCrudService<SysDepartment> {

    List<SysDepartment> tree();
}
