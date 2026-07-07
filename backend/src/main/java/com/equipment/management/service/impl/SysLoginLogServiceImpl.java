package com.equipment.management.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.equipment.management.common.constant.ErrorCode;
import com.equipment.management.common.exception.BusinessException;
import com.equipment.management.common.result.PageResult;
import com.equipment.management.common.util.PageUtils;
import com.equipment.management.dto.request.LogQuery;
import com.equipment.management.entity.SysLoginLog;
import com.equipment.management.mapper.SysLoginLogMapper;
import com.equipment.management.service.SysLoginLogService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class SysLoginLogServiceImpl extends ServiceImpl<SysLoginLogMapper, SysLoginLog>
        implements SysLoginLogService {

    @Override
    public PageResult<SysLoginLog> pageQuery(LogQuery query) {
        LambdaQueryWrapper<SysLoginLog> wrapper = Wrappers.lambdaQuery();
        wrapper.like(StringUtils.hasText(query.getUsername()), SysLoginLog::getUsername, query.getUsername())
                .orderByDesc(SysLoginLog::getLoginTime);
        return PageUtils.toPageResult(page(PageUtils.buildPage(query), wrapper));
    }

    @Override
    public SysLoginLog getDetail(Long id) {
        SysLoginLog log = getById(id);
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
