package com.equipment.management.service;

import com.equipment.management.common.service.BaseCrudService;
import com.equipment.management.entity.SysConfig;

import java.util.List;

public interface SysConfigService extends BaseCrudService<SysConfig> {

    List<SysConfig> listAll();

    void batchUpdate(List<SysConfig> configs);
}
