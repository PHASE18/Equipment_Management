package com.equipment.management.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.equipment.management.entity.SysDepartment;
import com.equipment.management.mapper.SysDepartmentMapper;
import com.equipment.management.common.service.impl.BaseCrudServiceImpl;
import com.equipment.management.service.SysDepartmentService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
/** 部门树服务实现。 */
public class SysDepartmentServiceImpl extends BaseCrudServiceImpl<SysDepartmentMapper, SysDepartment>
        implements SysDepartmentService {

    @Override
    public List<SysDepartment> tree() {
        return list(Wrappers.<SysDepartment>lambdaQuery()
                .orderByAsc(SysDepartment::getParentId)
                .orderByAsc(SysDepartment::getId));
    }
}
