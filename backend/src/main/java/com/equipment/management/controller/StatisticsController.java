package com.equipment.management.controller;

import com.equipment.management.annotation.RequireAuth;
import com.equipment.management.common.result.Result;
import com.equipment.management.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequireAuth
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/home")
    public Result<Map<String, Object>> home() {
        return Result.success(statisticsService.homeStatistics());
    }

    @GetMapping("/brand")
    public Result<List<Map<String, Object>>> brand() {
        return Result.success(statisticsService.brandStatistics());
    }

    @GetMapping("/type")
    public Result<List<Map<String, Object>>> type() {
        return Result.success(statisticsService.typeStatistics());
    }

    @GetMapping("/status")
    public Result<List<Map<String, Object>>> status() {
        return Result.success(statisticsService.statusStatistics());
    }

    @GetMapping("/fault")
    public Result<List<Map<String, Object>>> fault() {
        return Result.success(statisticsService.faultStatistics());
    }

    @GetMapping("/faultRank")
    public Result<List<Map<String, Object>>> faultRank() {
        return Result.success(statisticsService.faultRank());
    }

    @GetMapping("/cost")
    public Result<List<Map<String, Object>>> cost() {
        return Result.success(statisticsService.costStatistics());
    }
}
