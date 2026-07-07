package com.equipment.management.service;

import com.equipment.management.common.service.BaseCrudService;
import com.equipment.management.entity.SysDict;

import java.util.List;

public interface DictService extends BaseCrudService<SysDict> {

    List<SysDict> listByType(String type);
}
