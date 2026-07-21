package com.equipment.management.mapper;

import com.equipment.management.dto.StatisticsFilter;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface StatisticsMapper { // 统计Mapper

    Map<String, Object> selectDeviceSummary(StatisticsFilter filter); // 查询设备摘要

    Long selectMonthMaintenanceCount(StatisticsFilter filter); // 查询月度维修次数

    List<Map<String, Object>> selectStatusDistribution(StatisticsFilter filter); // 查询状态分布

    List<Map<String, Object>> selectBrandDistribution(StatisticsFilter filter); // 查询品牌分布

    List<Map<String, Object>> selectTypeDistribution(StatisticsFilter filter); // 查询类型分布

    List<Map<String, Object>> selectDepartmentDistribution(StatisticsFilter filter); // 查询部门分布

    List<Map<String, Object>> selectProjectDistribution(StatisticsFilter filter); // 查询项目分布

    List<Map<String, Object>> selectFaultDistribution(StatisticsFilter filter); // 查询故障分布

    List<Map<String, Object>> selectMaintenanceTrend(StatisticsFilter filter); // 查询维修趋势

    List<Map<String, Object>> selectMaintenanceCostTrend(StatisticsFilter filter); // 查询维修成本趋势

    List<Map<String, Object>> selectWarrantyDistribution(StatisticsFilter filter); // 查询保修分布      

    List<Map<String, Object>> selectSupplierDistribution(StatisticsFilter filter); // 查询供应商分布

    List<Map<String, Object>> selectMaintenanceCompanyDistribution(StatisticsFilter filter); // 查询维修公司分布

    List<Map<String, Object>> selectModelRanking(StatisticsFilter filter); // 查询型号排名

    List<Map<String, Object>> selectScrapDistribution(StatisticsFilter filter); // 查询报废分布 
}
