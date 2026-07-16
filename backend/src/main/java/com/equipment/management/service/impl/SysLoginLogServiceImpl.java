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
        // 登录日志表无 create_time，避免 PageQuery 默认排序字段导致 SQL 报错
        if (!StringUtils.hasText(query.getSortField()) || "createTime".equals(query.getSortField())) {
            query.setSortField("loginTime");
        }
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
        // 日志永久留存，禁止删除
        throw new BusinessException(ErrorCode.FORBIDDEN, "日志禁止删除");
    }
}
