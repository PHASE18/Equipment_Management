package com.equipment.management.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.equipment.management.common.constant.ErrorCode;
import com.equipment.management.common.exception.BusinessException;
import com.equipment.management.common.result.PageResult;
import com.equipment.management.common.util.PageUtils;
import com.equipment.management.dto.request.LogQuery;
import com.equipment.management.entity.SysOperationLog;
import com.equipment.management.mapper.SysOperationLogMapper;
import com.equipment.management.service.SysOperationLogService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class SysOperationLogServiceImpl extends ServiceImpl<SysOperationLogMapper, SysOperationLog>
        implements SysOperationLogService {

    @Override
    public PageResult<SysOperationLog> pageQuery(LogQuery query) {
        LambdaQueryWrapper<SysOperationLog> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(StringUtils.hasText(query.getOperationType()), SysOperationLog::getOperationType, query.getOperationType())
                .eq(StringUtils.hasText(query.getTableName()), SysOperationLog::getTableName, query.getTableName())
                .orderByDesc(SysOperationLog::getCreateTime);
        return PageUtils.toPageResult(page(PageUtils.buildPage(query), wrapper));
    }

    @Override
    public SysOperationLog getDetail(Long id) {
        SysOperationLog log = getById(id);
        if (log == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        return log;
    }

    @Override
    public void removeLog(Long id) {
        if (!removeById(id)) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
    }
}
