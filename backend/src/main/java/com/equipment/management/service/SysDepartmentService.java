package com.equipment.management.service;

import com.equipment.management.common.service.BaseCrudService;
import com.equipment.management.entity.SysDepartment;

import java.util.List;

public interface SysDepartmentService extends BaseCrudService<SysDepartment> {

    List<SysDepartment> tree();
}
