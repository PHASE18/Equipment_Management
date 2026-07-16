export interface BaseEntity {
  id?: number
  createTime?: string
  updateTime?: string
}

export interface Device extends BaseEntity {
  deviceNo: string
  deviceName: string
  sn?: string
  assetNo?: string
  brandCode?: string
  model?: string
  deviceTypeCode?: string
  departmentId?: number
  managerUserId?: number
  supplier?: string
  maintenanceCompany?: string
  purchaseDate?: string
  warrantyEnd?: string
  statusCode: string
  cabinet?: string
  location?: string
  remark?: string
}

export interface DeviceIp extends BaseEntity {
  deviceId?: number
  businessIp?: string
  managementIp?: string
  mask?: string
  gateway?: string
}

export interface Project extends BaseEntity {
  projectName: string
  projectCode: string
  departmentId?: number
  remark?: string
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

export interface DeviceStatusLogItem {
  id: number
  deviceId: number
  oldStatusCode?: string
  oldStatusName?: string
  newStatusCode: string
  newStatusName?: string
  changeReason?: string
  remark?: string
  operatorId?: number
  operatorName?: string
  changeTime?: string
}

export interface DeviceStatusChangeResult {
  deviceId: number
  oldStatusCode: string
  oldStatusName?: string
  newStatusCode: string
  newStatusName?: string
  allowedNextStatuses?: string[]
  history: DeviceStatusLogItem[]
}

export const DEVICE_STATUS_OPTIONS: DeviceStatusOption[] = [
  { label: '采购中', value: 'PURCHASING', type: 'info' },
  { label: '库存', value: 'IN_STOCK', type: 'primary' },
  { label: '待上架', value: 'PENDING_ONLINE', type: 'warning' },
  { label: '在用', value: 'IN_USE', type: 'success' },
  { label: '维修中', value: 'MAINTAINING', type: 'warning' },
  { label: '备用', value: 'STANDBY', type: 'info' },
  { label: '停用', value: 'STOPPED', type: 'info' },
  { label: '报废', value: 'SCRAPPED', type: 'danger' }
]

export function getDeviceStatusLabel(code?: string) {
  return DEVICE_STATUS_OPTIONS.find(item => item.value === code)?.label || code || '-'
}

export function getDeviceStatusType(code?: string): DeviceStatusOption['type'] {
  return DEVICE_STATUS_OPTIONS.find(item => item.value === code)?.type || 'info'
}
