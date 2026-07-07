package com.equipment.management.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.equipment.management.entity.SysDict;
import com.equipment.management.mapper.SysDictMapper;
import com.equipment.management.common.service.impl.BaseCrudServiceImpl;
import com.equipment.management.service.DictService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DictServiceImpl extends BaseCrudServiceImpl<SysDictMapper, SysDict> implements DictService {

    @Override
    public List<SysDict> listByType(String type) {
        return list(Wrappers.<SysDict>lambdaQuery()
                .eq(SysDict::getDictType, type)
                .eq(SysDict::getStatus, 1)
                .orderByAsc(SysDict::getSort));
    }
}
