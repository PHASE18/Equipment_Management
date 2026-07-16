package com.equipment.management.service.impl;

import com.equipment.management.common.context.UserContext;
import com.equipment.management.common.util.StatisticsFilterBuilder;
import com.equipment.management.dto.StatisticsFilter;
import com.equipment.management.dto.request.StatisticsQuery;
import com.equipment.management.dto.response.DashboardResponse;
import com.equipment.management.mapper.StatisticsMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceImplTest {

    @Mock
    private StatisticsMapper statisticsMapper;

    private StatisticsServiceImpl statisticsService;

    @BeforeEach
    void setUp() {
        statisticsService = new StatisticsServiceImpl(statisticsMapper);
        UserContext.LoginUser admin = new UserContext.LoginUser();
        admin.setUserId(1L);
        admin.setUsername("admin");
        admin.setDataScope(UserContext.DataScope.ALL);
        UserContext.set(admin);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    @DisplayName("dashboard 应聚合摘要与全部图表数据")
    void dashboard_shouldAggregateAllCharts() {
        Map<String, Object> summary = new HashMap<>();
        summary.put("deviceTotal", 10);
        summary.put("inUseCount", 6);
        summary.put("maintainingCount", 1);
        summary.put("stoppedCount", 1);
        summary.put("scrappedCount", 2);
        summary.put("warrantyExpiringCount", 3);
        summary.put("monthNewDeviceCount", 4);
        when(statisticsMapper.selectDeviceSummary(any())).thenReturn(summary);
        when(statisticsMapper.selectMonthMaintenanceCount(any())).thenReturn(5L);
        when(statisticsMapper.selectStatusDistribution(any()))
                .thenReturn(List.of(Map.of("code", "IN_USE", "name", "在用", "value", 6)));
        when(statisticsMapper.selectBrandDistribution(any())).thenReturn(List.of());
        when(statisticsMapper.selectTypeDistribution(any())).thenReturn(List.of());
        when(statisticsMapper.selectDepartmentDistribution(any())).thenReturn(List.of());
        when(statisticsMapper.selectProjectDistribution(any())).thenReturn(List.of());
        when(statisticsMapper.selectFaultDistribution(any())).thenReturn(List.of());
        when(statisticsMapper.selectMaintenanceTrend(any())).thenReturn(List.of());
        when(statisticsMapper.selectMaintenanceCostTrend(any()))
                .thenReturn(List.of(Map.of("code", "2026-07", "name", "2026-07", "value", 2, "amount", new BigDecimal("100.5"))));
        when(statisticsMapper.selectWarrantyDistribution(any())).thenReturn(List.of());
        when(statisticsMapper.selectSupplierDistribution(any()))
                .thenReturn(List.of(Map.of("code", "华为", "name", "华为", "value", 3)));
        when(statisticsMapper.selectMaintenanceCompanyDistribution(any())).thenReturn(List.of());
        when(statisticsMapper.selectModelRanking(any())).thenReturn(List.of());
        when(statisticsMapper.selectScrapDistribution(any())).thenReturn(List.of());

        DashboardResponse response = statisticsService.dashboard(new StatisticsQuery());

        assertEquals(10L, response.getSummary().getDeviceTotal());
        assertEquals(5L, response.getSummary().getMonthMaintenanceCount());
        assertEquals(1, response.getStatusChart().size());
        assertEquals("在用", response.getStatusChart().get(0).getName());
        assertEquals(1, response.getSupplierChart().size());
        assertEquals(new BigDecimal("100.5"), response.getMaintenanceCostChart().get(0).getAmount());
    }

    @Test
    @DisplayName("homeStatistics 应返回卡片字段")
    void homeStatistics_shouldReturnSummaryFields() {
        Map<String, Object> summary = new HashMap<>();
        summary.put("deviceTotal", 8);
        summary.put("inUseCount", 5);
        summary.put("maintainingCount", 1);
        summary.put("stoppedCount", 1);
        summary.put("scrappedCount", 1);
        summary.put("warrantyExpiringCount", 0);
        summary.put("monthNewDeviceCount", 2);
        when(statisticsMapper.selectDeviceSummary(any())).thenReturn(summary);
        when(statisticsMapper.selectMonthMaintenanceCount(any())).thenReturn(3L);
        when(statisticsMapper.selectStatusDistribution(any())).thenReturn(List.of());
        when(statisticsMapper.selectBrandDistribution(any())).thenReturn(List.of());
        when(statisticsMapper.selectTypeDistribution(any())).thenReturn(List.of());
        when(statisticsMapper.selectDepartmentDistribution(any())).thenReturn(List.of());
        when(statisticsMapper.selectProjectDistribution(any())).thenReturn(List.of());
        when(statisticsMapper.selectFaultDistribution(any())).thenReturn(List.of());
        when(statisticsMapper.selectMaintenanceTrend(any())).thenReturn(List.of());
        when(statisticsMapper.selectMaintenanceCostTrend(any())).thenReturn(List.of());
        when(statisticsMapper.selectWarrantyDistribution(any())).thenReturn(List.of());
        when(statisticsMapper.selectSupplierDistribution(any())).thenReturn(List.of());
        when(statisticsMapper.selectMaintenanceCompanyDistribution(any())).thenReturn(List.of());
        when(statisticsMapper.selectModelRanking(any())).thenReturn(List.of());
        when(statisticsMapper.selectScrapDistribution(any())).thenReturn(List.of());

        Map<String, Object> home = statisticsService.homeStatistics(new StatisticsQuery());

        assertEquals(8L, home.get("deviceTotal"));
        assertEquals(3L, home.get("monthMaintenanceCount"));
        assertEquals(3L, home.get("maintenanceTotal"));
    }

    @Test
    @DisplayName("brandStatistics 应透传筛选条件")
    void brandStatistics_shouldPassFilter() {
        StatisticsQuery query = new StatisticsQuery();
        query.setBrandCode("HUAWEI");
        query.setDepartmentId(2L);
        when(statisticsMapper.selectBrandDistribution(any())).thenReturn(List.of());

        statisticsService.brandStatistics(query);

        ArgumentCaptor<StatisticsFilter> captor = ArgumentCaptor.forClass(StatisticsFilter.class);
        verify(statisticsMapper).selectBrandDistribution(captor.capture());
        assertEquals("HUAWEI", captor.getValue().getBrandCode());
        assertEquals(2L, captor.getValue().getDepartmentId());
        assertTrue(captor.getValue().isAllScope());
    }

    @Nested
    @DisplayName("StatisticsFilterBuilder")
    class FilterBuilderTests {

        @AfterEach
        void clear() {
            UserContext.clear();
        }

        @Test
        @DisplayName("全部数据权限用户应 allScope=true")
        void allScopeUser() {
            UserContext.LoginUser user = new UserContext.LoginUser();
            user.setDataScope(UserContext.DataScope.ALL);
            UserContext.set(user);

            StatisticsFilter filter = StatisticsFilterBuilder.from(new StatisticsQuery());
            assertTrue(filter.isAllScope());
        }

        @Test
        @DisplayName("部门数据权限应写入 scopeDepartmentId")
        void departmentScopeUser() {
            UserContext.LoginUser user = new UserContext.LoginUser();
            user.setDepartmentId(9L);
            user.setDataScope(UserContext.DataScope.DEPARTMENT);
            UserContext.set(user);

            StatisticsFilter filter = StatisticsFilterBuilder.from(new StatisticsQuery());
            assertFalse(filter.isAllScope());
            assertEquals(9L, filter.getScopeDepartmentId());
        }

        @Test
        @DisplayName("本人数据权限应写入 scopeUserId")
        void selfScopeUser() {
            UserContext.LoginUser user = new UserContext.LoginUser();
            user.setUserId(77L);
            user.setDataScope(UserContext.DataScope.SELF);
            UserContext.set(user);

            StatisticsFilter filter = StatisticsFilterBuilder.from(new StatisticsQuery());
            assertFalse(filter.isAllScope());
            assertEquals(77L, filter.getScopeUserId());
        }
    }
}
