import http from '@/api/http'
import type { DashboardData, StatisticsQuery } from '@/types/statistics'

export function getDashboardApi(params?: StatisticsQuery) {
  return http.get<DashboardData, DashboardData>('/statistics/dashboard', { params })
}

export function getHomeStatisticsApi(params?: StatisticsQuery) {
  return http.get<Record<string, number>, Record<string, number>>('/statistics/home', { params })
}
