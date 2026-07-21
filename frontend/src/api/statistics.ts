/** 首页仪表盘及统计图表接口封装。 */
import http from '@/api/http'
import type { ChartItem, DashboardData, StatisticsQuery } from '@/types/statistics'

//携带页面筛选条件（部门、项目、时间等）调用后端 /statistics/dashboard 接口，返回大盘图表、统计数字数据。
export function getDashboardApi(params?: StatisticsQuery) {
  return http.get<DashboardData, DashboardData>('/statistics/dashboard', { params })
}

export function getHomeStatisticsApi(params?: StatisticsQuery) {
  return http.get<Record<string, number>, Record<string, number>>('/statistics/home', { params })
}

export function getBrandStatisticsApi(params?: StatisticsQuery) {
  return http.get<ChartItem[], ChartItem[]>('/statistics/brand', { params })
}

export function getTypeStatisticsApi(params?: StatisticsQuery) {
  return http.get<ChartItem[], ChartItem[]>('/statistics/type', { params })
}

export function getStatusStatisticsApi(params?: StatisticsQuery) {
  return http.get<ChartItem[], ChartItem[]>('/statistics/status', { params })
}

export function getDepartmentStatisticsApi(params?: StatisticsQuery) {
  return http.get<ChartItem[], ChartItem[]>('/statistics/department', { params })
}

export function getProjectStatisticsApi(params?: StatisticsQuery) {
  return http.get<ChartItem[], ChartItem[]>('/statistics/project', { params })
}

export function getFaultStatisticsApi(params?: StatisticsQuery) {
  return http.get<ChartItem[], ChartItem[]>('/statistics/fault', { params })
}

export function getMaintenanceTrendApi(params?: StatisticsQuery) {
  return http.get<ChartItem[], ChartItem[]>('/statistics/maintenance-trend', { params })
}

export function getCostStatisticsApi(params?: StatisticsQuery) {
  return http.get<ChartItem[], ChartItem[]>('/statistics/cost', { params })
}

export function getWarrantyStatisticsApi(params?: StatisticsQuery) {
  return http.get<ChartItem[], ChartItem[]>('/statistics/warranty', { params })
}

export function getSupplierStatisticsApi(params?: StatisticsQuery) {
  return http.get<ChartItem[], ChartItem[]>('/statistics/supplier', { params })
}

export function getMaintenanceCompanyStatisticsApi(params?: StatisticsQuery) {
  return http.get<ChartItem[], ChartItem[]>('/statistics/maintenance-company', { params })
}

export function getModelStatisticsApi(params?: StatisticsQuery) {
  return http.get<ChartItem[], ChartItem[]>('/statistics/model', { params })
}

export function getScrapStatisticsApi(params?: StatisticsQuery) {
  return http.get<ChartItem[], ChartItem[]>('/statistics/scrap', { params })
}
