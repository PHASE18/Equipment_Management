import http from '@/api/http'
import type { PageQuery, PageResult } from '@/types/api'
import type { Device, LifecycleTransitionRequest } from '@/types/device'

export interface DeviceQuery extends PageQuery {
  deviceNo?: string
  deviceName?: string
  status?: string
  departmentId?: number
  keyword?: string
}

export function pageDevicesApi(params: DeviceQuery) {
  return http.get<PageResult<Device>, PageResult<Device>>('/devices', { params })
}

export function changeDeviceStatusApi(data: LifecycleTransitionRequest) {
  return http.post<void, void>('/device/status/change', data)
}

export function importDevicesApi(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return http.post('/excel/import/devices', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
