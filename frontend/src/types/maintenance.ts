import type { FileMeta } from '@/types/file'

export interface MaintenanceRecord {
  id?: number
  deviceId?: number
  deviceNo?: string
  deviceName?: string
  maintenanceDate: string
  maintenancePerson?: string
  maintenanceCompany?: string
  faultTypeCode?: string
  faultTypeName?: string
  faultReason?: string
  faultDescription?: string
  replaceParts?: string
  maintenanceCost?: number
  recoverDate?: string
  isResolved?: number
  attachmentPath?: string
  remark?: string
  createTime?: string
  attachments?: FileMeta[]
}

export interface MaintenanceQuery {
  pageNum: number
  pageSize: number
  keyword?: string
  deviceId?: number
  faultType?: string
  isResolved?: number
}

export interface FaultTypeStat {
  faultTypeCode: string
  faultTypeName: string
  count: number
}
