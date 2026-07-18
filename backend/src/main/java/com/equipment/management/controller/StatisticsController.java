package com.equipment.management.controller;

import com.equipment.management.annotation.RequireAuth;
import com.equipment.management.annotation.RequirePermission;
import com.equipment.management.common.result.Result;
import com.equipment.management.dto.request.StatisticsQuery;
import com.equipment.management.dto.response.DashboardResponse;
import com.equipment.management.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequireAuth
@RequirePermission(any = {"statistics:view", "dashboard:view"})
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
/** 首页汇总指标、趋势图和分类统计接口。 */
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/dashboard")
    public Result<DashboardResponse> dashboard(StatisticsQuery query) {
        return Result.success(statisticsService.dashboard(query));
    }

    @GetMapping("/home")
    public Result<Map<String, Object>> home(StatisticsQuery query) {
        return Result.success(statisticsService.homeStatistics(query));
    }

    @GetMapping("/brand")
    public Result<List<Map<String, Object>>> brand(StatisticsQuery query) {
        return Result.success(statisticsService.brandStatistics(query));
    }

    @GetMapping("/type")
    public Result<List<Map<String, Object>>> type(StatisticsQuery query) {
        return Result.success(statisticsService.typeStatistics(query));
    }

    @GetMapping("/status")
    public Result<List<Map<String, Object>>> status(StatisticsQuery query) {
        return Result.success(statisticsService.statusStatistics(query));
    }

    @GetMapping("/department")
    public Result<List<Map<String, Object>>> department(StatisticsQuery query) {
        return Result.success(statisticsService.departmentStatistics(query));
    }

    @GetMapping("/project")
    public Result<List<Map<String, Object>>> project(StatisticsQuery query) {
        return Result.success(statisticsService.projectStatistics(query));
    }

    @GetMapping("/fault")
    public Result<List<Map<String, Object>>> fault(StatisticsQuery query) {
        return Result.success(statisticsService.faultStatistics(query));
    }

    @GetMapping("/faultRank")
    public Result<List<Map<String, Object>>> faultRank(StatisticsQuery query) {
        return Result.success(statisticsService.faultRank(query));
    }

    @GetMapping("/maintenance-trend")
    public Result<List<Map<String, Object>>> maintenanceTrend(StatisticsQuery query) {
        return Result.success(statisticsService.maintenanceTrend(query));
    }

    @GetMapping("/cost")
    public Result<List<Map<String, Object>>> cost(StatisticsQuery query) {
        return Result.success(statisticsService.costStatistics(query));
    }

    @GetMapping("/warranty")
    public Result<List<Map<String, Object>>> warranty(StatisticsQuery query) {
        return Result.success(statisticsService.warrantyStatistics(query));
    }

    @GetMapping("/supplier")
    public Result<List<Map<String, Object>>> supplier(StatisticsQuery query) {
        return Result.success(statisticsService.supplierStatistics(query));
    }

    @GetMapping("/maintenance-company")
    public Result<List<Map<String, Object>>> maintenanceCompany(StatisticsQuery query) {
        return Result.success(statisticsService.maintenanceCompanyStatistics(query));
    }

    @GetMapping("/model")
    public Result<List<Map<String, Object>>> model(StatisticsQuery query) {
        return Result.success(statisticsService.modelStatistics(query));
    }

    @GetMapping("/scrap")
    public Result<List<Map<String, Object>>> scrap(StatisticsQuery query) {
        return Result.success(statisticsService.scrapStatistics(query));
    }
}
