package com.equipment.management.service;

import com.equipment.management.dto.request.StatisticsQuery;
import com.equipment.management.dto.response.DashboardResponse;

import java.util.List;
import java.util.Map;

public interface StatisticsService {

    DashboardResponse dashboard(StatisticsQuery query);

    Map<String, Object> homeStatistics(StatisticsQuery query);

    List<Map<String, Object>> brandStatistics(StatisticsQuery query);

    List<Map<String, Object>> typeStatistics(StatisticsQuery query);

    List<Map<String, Object>> statusStatistics(StatisticsQuery query);

    List<Map<String, Object>> faultStatistics(StatisticsQuery query);

    List<Map<String, Object>> faultRank(StatisticsQuery query);

    List<Map<String, Object>> costStatistics(StatisticsQuery query);

    List<Map<String, Object>> departmentStatistics(StatisticsQuery query);

    List<Map<String, Object>> projectStatistics(StatisticsQuery query);

    List<Map<String, Object>> maintenanceTrend(StatisticsQuery query);

    List<Map<String, Object>> warrantyStatistics(StatisticsQuery query);

    List<Map<String, Object>> supplierStatistics(StatisticsQuery query);

    List<Map<String, Object>> maintenanceCompanyStatistics(StatisticsQuery query);

    List<Map<String, Object>> modelStatistics(StatisticsQuery query);

    List<Map<String, Object>> scrapStatistics(StatisticsQuery query);
}
