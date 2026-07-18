package com.equipment.management.service.impl;

import com.equipment.management.common.util.StatisticsFilterBuilder;
import com.equipment.management.dto.StatisticsFilter;
import com.equipment.management.dto.request.StatisticsQuery;
import com.equipment.management.dto.response.DashboardResponse;
import com.equipment.management.mapper.StatisticsMapper;
import com.equipment.management.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
/** 统计服务实现，组合筛选条件并调用统计查询。 */
public class StatisticsServiceImpl implements StatisticsService {

    private final StatisticsMapper statisticsMapper;

    @Override
    public DashboardResponse dashboard(StatisticsQuery query) {
        StatisticsFilter filter = StatisticsFilterBuilder.from(query);
        Map<String, Object> summaryMap = statisticsMapper.selectDeviceSummary(filter);
        Long monthMaintenanceCount = statisticsMapper.selectMonthMaintenanceCount(filter);

        return DashboardResponse.builder()
                .summary(buildSummary(summaryMap, monthMaintenanceCount))
                .statusChart(toChartItems(statisticsMapper.selectStatusDistribution(filter)))
                .brandChart(toChartItems(statisticsMapper.selectBrandDistribution(filter)))
                .typeChart(toChartItems(statisticsMapper.selectTypeDistribution(filter)))
                .departmentChart(toChartItems(statisticsMapper.selectDepartmentDistribution(filter)))
                .projectChart(toChartItems(statisticsMapper.selectProjectDistribution(filter)))
                .faultChart(toChartItems(statisticsMapper.selectFaultDistribution(filter)))
                .maintenanceTrendChart(toChartItems(statisticsMapper.selectMaintenanceTrend(filter)))
                .maintenanceCostChart(toChartItems(statisticsMapper.selectMaintenanceCostTrend(filter)))
                .warrantyChart(toChartItems(statisticsMapper.selectWarrantyDistribution(filter)))
                .supplierChart(toChartItems(statisticsMapper.selectSupplierDistribution(filter)))
                .maintenanceCompanyChart(toChartItems(statisticsMapper.selectMaintenanceCompanyDistribution(filter)))
                .modelChart(toChartItems(statisticsMapper.selectModelRanking(filter)))
                .scrapChart(toChartItems(statisticsMapper.selectScrapDistribution(filter)))
                .build();
    }

    @Override
    public Map<String, Object> homeStatistics(StatisticsQuery query) {
        DashboardResponse.DashboardSummary summary = dashboard(query).getSummary();
        Map<String, Object> data = new HashMap<>();
        data.put("deviceTotal", summary.getDeviceTotal());
        data.put("inUseCount", summary.getInUseCount());
        data.put("maintainingCount", summary.getMaintainingCount());
        data.put("stoppedCount", summary.getStoppedCount());
        data.put("scrappedCount", summary.getScrappedCount());
        data.put("warrantyExpiringCount", summary.getWarrantyExpiringCount());
        data.put("monthNewDeviceCount", summary.getMonthNewDeviceCount());
        data.put("monthMaintenanceCount", summary.getMonthMaintenanceCount());
        data.put("maintenanceTotal", summary.getMonthMaintenanceCount());
        return data;
    }

    @Override
    public List<Map<String, Object>> brandStatistics(StatisticsQuery query) {
        return statisticsMapper.selectBrandDistribution(StatisticsFilterBuilder.from(query));
    }

    @Override
    public List<Map<String, Object>> typeStatistics(StatisticsQuery query) {
        return statisticsMapper.selectTypeDistribution(StatisticsFilterBuilder.from(query));
    }

    @Override
    public List<Map<String, Object>> statusStatistics(StatisticsQuery query) {
        return statisticsMapper.selectStatusDistribution(StatisticsFilterBuilder.from(query));
    }

    @Override
    public List<Map<String, Object>> faultStatistics(StatisticsQuery query) {
        return statisticsMapper.selectFaultDistribution(StatisticsFilterBuilder.from(query));
    }

    @Override
    public List<Map<String, Object>> faultRank(StatisticsQuery query) {
        return faultStatistics(query);
    }

    @Override
    public List<Map<String, Object>> costStatistics(StatisticsQuery query) {
        return statisticsMapper.selectMaintenanceCostTrend(StatisticsFilterBuilder.from(query));
    }

    @Override
    public List<Map<String, Object>> departmentStatistics(StatisticsQuery query) {
        return statisticsMapper.selectDepartmentDistribution(StatisticsFilterBuilder.from(query));
    }

    @Override
    public List<Map<String, Object>> projectStatistics(StatisticsQuery query) {
        return statisticsMapper.selectProjectDistribution(StatisticsFilterBuilder.from(query));
    }

    @Override
    public List<Map<String, Object>> maintenanceTrend(StatisticsQuery query) {
        return statisticsMapper.selectMaintenanceTrend(StatisticsFilterBuilder.from(query));
    }

    @Override
    public List<Map<String, Object>> warrantyStatistics(StatisticsQuery query) {
        return statisticsMapper.selectWarrantyDistribution(StatisticsFilterBuilder.from(query));
    }

    @Override
    public List<Map<String, Object>> supplierStatistics(StatisticsQuery query) {
        return statisticsMapper.selectSupplierDistribution(StatisticsFilterBuilder.from(query));
    }

    @Override
    public List<Map<String, Object>> maintenanceCompanyStatistics(StatisticsQuery query) {
        return statisticsMapper.selectMaintenanceCompanyDistribution(StatisticsFilterBuilder.from(query));
    }

    @Override
    public List<Map<String, Object>> modelStatistics(StatisticsQuery query) {
        return statisticsMapper.selectModelRanking(StatisticsFilterBuilder.from(query));
    }

    @Override
    public List<Map<String, Object>> scrapStatistics(StatisticsQuery query) {
        return statisticsMapper.selectScrapDistribution(StatisticsFilterBuilder.from(query));
    }

    private DashboardResponse.DashboardSummary buildSummary(Map<String, Object> summaryMap, Long monthMaintenanceCount) {
        Map<String, Object> safeMap = summaryMap != null ? summaryMap : Collections.emptyMap();
        return DashboardResponse.DashboardSummary.builder()
                .deviceTotal(toLong(safeMap.get("deviceTotal")))
                .inUseCount(toLong(safeMap.get("inUseCount")))
                .maintainingCount(toLong(safeMap.get("maintainingCount")))
                .stoppedCount(toLong(safeMap.get("stoppedCount")))
                .scrappedCount(toLong(safeMap.get("scrappedCount")))
                .warrantyExpiringCount(toLong(safeMap.get("warrantyExpiringCount")))
                .monthNewDeviceCount(toLong(safeMap.get("monthNewDeviceCount")))
                .monthMaintenanceCount(monthMaintenanceCount != null ? monthMaintenanceCount : 0L)
                .build();
    }

    private List<DashboardResponse.ChartItem> toChartItems(List<Map<String, Object>> rows) {
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }
        return rows.stream()
                .map(row -> DashboardResponse.ChartItem.builder()
                        .code(row.get("code") != null ? String.valueOf(row.get("code")) : null)
                        .name(row.get("name") != null ? String.valueOf(row.get("name")) : null)
                        .value(toLong(row.get("value")))
                        .amount(toBigDecimal(row.get("amount")))
                        .build())
                .toList();
    }

    private Long toLong(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        return new BigDecimal(String.valueOf(value));
    }
}
