package com.equipment.management.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class DashboardResponse {

    private DashboardSummary summary;
    private List<ChartItem> statusChart;
    private List<ChartItem> brandChart;
    private List<ChartItem> typeChart;
    private List<ChartItem> departmentChart;
    private List<ChartItem> projectChart;
    private List<ChartItem> faultChart;
    private List<ChartItem> maintenanceTrendChart;
    private List<ChartItem> maintenanceCostChart;
    private List<ChartItem> warrantyChart;

    @Data
    @Builder
    public static class DashboardSummary {
        private Long deviceTotal;
        private Long inUseCount;
        private Long maintainingCount;
        private Long stoppedCount;
        private Long scrappedCount;
        private Long warrantyExpiringCount;
        private Long monthNewDeviceCount;
        private Long monthMaintenanceCount;
    }

    @Data
    @Builder
    public static class ChartItem {
        private String code;
        private String name;
        private Long value;
        private BigDecimal amount;
    }
}
