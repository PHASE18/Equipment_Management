import axios from 'axios'
import http from '@/api/http'
import type { PageQuery, PageResult } from '@/types/api'
import type { Device, DeviceIp, DeviceStatusChangeResult, DeviceStatusLogItem, LifecycleTransitionRequest } from '@/types/device'
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

export async function exportDevicesApi(params: DeviceQuery) {
  const token = getToken()
  const response = await axios.get('/api/excel/export', {
    params,
    responseType: 'blob',
    headers: token ? { Authorization: `Bearer ${token}` } : undefined
  })
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
