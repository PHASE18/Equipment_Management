package com.equipment.management.service;

import java.util.List;
import java.util.Map;

public interface StatisticsService {

    Map<String, Object> homeStatistics();

    List<Map<String, Object>> brandStatistics();

    List<Map<String, Object>> typeStatistics();

    List<Map<String, Object>> statusStatistics();

    List<Map<String, Object>> faultStatistics();

    List<Map<String, Object>> faultRank();

    List<Map<String, Object>> costStatistics();
}
