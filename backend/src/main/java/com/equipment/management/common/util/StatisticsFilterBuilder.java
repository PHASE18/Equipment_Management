package com.equipment.management.common.util;

import com.equipment.management.common.context.UserContext;
import com.equipment.management.dto.StatisticsFilter;
import com.equipment.management.dto.request.StatisticsQuery;

public final class StatisticsFilterBuilder {

    private StatisticsFilterBuilder() {
    }

    public static StatisticsFilter from(StatisticsQuery query) {
        StatisticsFilter filter = new StatisticsFilter();
        if (query != null) {
            filter.setDepartmentId(query.getDepartmentId());
            filter.setProjectId(query.getProjectId());
            filter.setBrandCode(query.getBrandCode());
            filter.setDeviceTypeCode(query.getDeviceTypeCode());
            filter.setStartDate(query.getStartDate());
            filter.setEndDate(query.getEndDate());
        }
        UserContext.LoginUser user = UserContext.get();
        if (user == null || user.isAllDataScope()) {
            filter.setAllScope(true);
            return filter;
        }
        filter.setAllScope(false);
        if (user.isDepartmentDataScope()) {
            filter.setScopeDepartmentId(user.getDepartmentId());
        } else {
            filter.setScopeUserId(user.getUserId());
        }
        return filter;
    }
}
