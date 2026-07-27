/** 设备、设备 IP、状态和生命周期相关的后端接口封装。 */
import axios from 'axios'
import http from '@/api/http'
import type { PageQuery, PageResult } from '@/types/api'
import type {
  Device,
  DeviceConfig,
  DeviceIp,
  DeviceMigrateRequest,
  DeviceMigrateResult,
  DeviceMigrationLog,
  DeviceStatusChangeResult,
  DeviceStatusLogItem,
  LifecycleTransitionRequest
} from '@/types/device'
import { getToken } from '@/utils/token'

export interface DeviceQuery extends PageQuery {
  deviceNo?: string
  deviceName?: string
  brand?: string
  model?: string
  status?: string
  departmentId?: number
  projectId?: number
  keyword?: string
}

export function pageDevicesApi(params: DeviceQuery) {
  return http.get<PageResult<Device>, PageResult<Device>>('/device/list', { params })
}

export function getDeviceApi(id: number) {
  return http.get<Device, Device>(`/device/${id}`)
}

export function createDeviceApi(data: Partial<Device>) {
  return http.post<void, void>('/device', data)
}

export function updateDeviceApi(data: Partial<Device>) {
  return http.put<void, void>('/device', data)
}

export function deleteDeviceApi(id: number) {
  return http.delete<void, void>(`/device/${id}`)
}

export function getDeviceIpApi(deviceId: number) {
  return http.get<DeviceIp | null, DeviceIp | null>(`/ip/device/${deviceId}`)
}

export function saveDeviceIpApi(data: DeviceIp) {
  return http.post<void, void>('/ip/save', data)
}

export function getDeviceConfigApi(deviceId: number) {
  return http.get<DeviceConfig | null, DeviceConfig | null>(`/device-config/device/${deviceId}`)
}

export function saveDeviceConfigApi(data: DeviceConfig) {
  return http.post<void, void>('/device-config/save', data)
}

export function listDeviceProjectIdsApi(deviceId: number) {
  return http.get<number[], number[]>(`/device-project/by-device/${deviceId}`)
}

export function syncDeviceProjectsApi(deviceId: number, projectIds: number[]) {
  return http.post<void, void>('/device-project/sync', { deviceId, projectIds })
}

export function changeDeviceStatusApi(data: LifecycleTransitionRequest) {
  return http.post<DeviceStatusChangeResult, DeviceStatusChangeResult>('/device/status/change', data)
}

export function listDeviceStatusHistoryApi(deviceId: number) {
  return http.get<DeviceStatusLogItem[], DeviceStatusLogItem[]>(`/device/status/history/${deviceId}`)
}

export function listAllowedStatusTransitionsApi(deviceId: number) {
  return http.get<string[], string[]>(`/device/status/transitions/${deviceId}`)
}

export function migrateDeviceApi(data: DeviceMigrateRequest) {
  return http.post<DeviceMigrateResult, DeviceMigrateResult>('/device/migrate', data)
}

export function listDeviceMigrationHistoryApi(deviceId: number) {
  return http.get<DeviceMigrationLog[], DeviceMigrationLog[]>(`/device/migrate/history/${deviceId}`)
}

export function getDeviceMigrationDetailApi(id: number) {
  return http.get<DeviceMigrationLog, DeviceMigrationLog>(`/device/migrate/${id}`)
}

export async function exportDevicesApi(params: DeviceQuery) {
  const token = getToken()
  const response = await axios.get('/api/excel/export', {
    params,
    responseType: 'blob',
    headers: token ? { Authorization: `Bearer ${token}` } : undefined
  })

  const contentType = String(response.headers['content-type'] || '')
  if (contentType.includes('application/json')) {
    const text = await (response.data as Blob).text()
    let message = '导出失败'
    try {
      const body = JSON.parse(text) as { message?: string; code?: number }
      message = body.message || message
    } catch {
      message = text || message
    }
    throw new Error(message)
  }

  // Some gateways still return JSON with wrong content-type; sniff payload.
  const probe = await (response.data as Blob).slice(0, 1).text()
  if (probe === '{' || probe === '[') {
    const text = await (response.data as Blob).text()
    let message = '导出失败'
    try {
      const body = JSON.parse(text) as { message?: string }
      message = body.message || message
    } catch {
      message = text || message
    }
    throw new Error(message)
  }

  const blob = new Blob([response.data], {
    type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
  })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `devices_${Date.now()}.xlsx`
  link.click()
  URL.revokeObjectURL(url)
}

export async function importDevicesApi(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return http.post('/excel/import', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
