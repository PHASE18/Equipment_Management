package com.equipment.management.mapper;

import com.equipment.management.dto.StatisticsFilter;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface StatisticsMapper {

    Map<String, Object> selectDeviceSummary(StatisticsFilter filter);

    Long selectMonthMaintenanceCount(StatisticsFilter filter);

    List<Map<String, Object>> selectStatusDistribution(StatisticsFilter filter);

    List<Map<String, Object>> selectBrandDistribution(StatisticsFilter filter);

    List<Map<String, Object>> selectTypeDistribution(StatisticsFilter filter);

    List<Map<String, Object>> selectDepartmentDistribution(StatisticsFilter filter);

    List<Map<String, Object>> selectProjectDistribution(StatisticsFilter filter);

    List<Map<String, Object>> selectFaultDistribution(StatisticsFilter filter);

    List<Map<String, Object>> selectMaintenanceTrend(StatisticsFilter filter);

    List<Map<String, Object>> selectMaintenanceCostTrend(StatisticsFilter filter);

    List<Map<String, Object>> selectWarrantyDistribution(StatisticsFilter filter);
}
