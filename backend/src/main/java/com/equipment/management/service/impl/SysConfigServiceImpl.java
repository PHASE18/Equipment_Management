package com.equipment.management.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.equipment.management.entity.SysConfig;
import com.equipment.management.mapper.SysConfigMapper;
import com.equipment.management.common.service.impl.BaseCrudServiceImpl;
import com.equipment.management.service.SysConfigService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
/** 系统配置服务实现。 */
public class SysConfigServiceImpl extends BaseCrudServiceImpl<SysConfigMapper, SysConfig> implements SysConfigService {

    @Override
    public List<SysConfig> listAll() {
        return list(Wrappers.<SysConfig>lambdaQuery().orderByAsc(SysConfig::getConfigKey));
    }

    @Override
    public void batchUpdate(List<SysConfig> configs) {
        configs.forEach(this::updateEntity);
    }
}
