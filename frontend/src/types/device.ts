export interface Device {
  id: number
  deviceNo: string
  deviceName: string
  sn: string
  brandCode?: string
  model?: string
  deviceTypeCode?: string
  departmentId?: number
  managerUserId?: number
  statusCode: string
  location?: string
}

export interface DeviceStatusOption {
  label: string
  value: string
  type: 'info' | 'success' | 'warning' | 'danger' | 'primary'
}

export interface LifecycleTransitionRequest {
  deviceId: number
  fromStatus: string
  toStatus: string
  reason: string
  remark?: string
}
