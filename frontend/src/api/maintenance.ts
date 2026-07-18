/** 设备维修记录和故障类型统计接口封装。 */
import http from '@/api/http'
import type { PageResult } from '@/types/api'
import type { FaultTypeStat, MaintenanceQuery, MaintenanceRecord } from '@/types/maintenance'

export function pageMaintenanceApi(params: MaintenanceQuery) {
  return http.get<PageResult<MaintenanceRecord>, PageResult<MaintenanceRecord>>('/maintenance/list', { params })
}

export function getMaintenanceApi(id: number) {
  return http.get<MaintenanceRecord, MaintenanceRecord>(`/maintenance/detail/${id}`)
}

export function createMaintenanceApi(data: Partial<MaintenanceRecord>) {
  return http.post<number, number>('/maintenance/submit', data)
}

export function updateMaintenanceApi(data: Partial<MaintenanceRecord>) {
  return http.put<void, void>('/maintenance', data)
}

export function deleteMaintenanceApi(id: number) {
  return http.delete<void, void>(`/maintenance/${id}`)
}

export function completeMaintenanceApi(id: number) {
  return http.put<void, void>(`/maintenance/${id}/complete`)
}

export function faultTypeStatsApi() {
  return http.get<FaultTypeStat[], FaultTypeStat[]>('/maintenance/fault-stats')
}

export function listFaultTypesApi() {
  return http.get<Array<{ dictCode: string; dictName: string }>, Array<{ dictCode: string; dictName: string }>>(
    '/dict/fault_type'
  )
}
