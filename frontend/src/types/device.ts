/** 与后端基础实体对应的公共审计字段。 */
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
  /** 是否固定资产：0-否 1-是 */
  isFixedAsset?: number
  brandCode?: string
  model?: string
  deviceTypeCode?: string
  /** 管理部门 */
  departmentId?: number
  /** 使用部门 */
  useDepartmentId?: number
  /** 责任人用户ID（数据权限，表单一般不编辑） */
  managerUserId?: number
  /** 责任人（自由文本） */
  managerName?: string
  /** 使用人 */
  useUserName?: string
  originalValue?: number
  approvalNo?: string
  supplier?: string
  maintenanceCompany?: string
  purchaseDate?: string
  manufactureDate?: string
  onlineDate?: string
  /** 到保日期 */
  warrantyEnd?: string
  scrapDate?: string
  statusCode: string
  /** 是否维修中：0-否 1-是（可与在用/停用并存） */
  maintainingFlag?: number
  /** 机柜U位 */
  cabinet?: string
  /** 所在机房 */
  location?: string
  remark?: string
}

export interface DeviceIp extends BaseEntity {
  deviceId?: number
  businessIp?: string
  managementIp?: string
  mask?: string
  gateway?: string
  mountedBusiness?: string
  networkZone?: string
  mgmtLoginMethod?: string
}

export interface DeviceConfig extends BaseEntity {
  deviceId?: number
  cpu?: string
  memory?: string
  disk?: string
  raid?: string
  gpu?: string
  fiberCard?: string
  nic?: string
  powerSupply?: string
  os?: string
  dbVersion?: string
  firmware?: string
  bios?: string
  remark?: string
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
  maintainingFlag?: number
  allowedNextStatuses?: string[]
  history: DeviceStatusLogItem[]
}

/** 设备迁移请求：前端回填当前值后提交，后端只落库有变更字段。 */
export interface DeviceMigrateRequest {
  deviceId: number
  projectIds?: number[]
  reason: string
  remark?: string
  departmentId?: number
  useDepartmentId?: number
  managerUserId?: number
  managerName?: string
  useUserName?: string
  location?: string
  cabinet?: string
  businessIp?: string
  managementIp?: string
  mask?: string
  gateway?: string
  mountedBusiness?: string
  networkZone?: string
  mgmtLoginMethod?: string
  cpu?: string
  memory?: string
  disk?: string
  raid?: string
  gpu?: string
  fiberCard?: string
  nic?: string
  powerSupply?: string
  os?: string
  dbVersion?: string
  firmware?: string
  bios?: string
  configRemark?: string
}

export interface DeviceMigrationItem {
  id?: number
  fieldGroup: string
  fieldKey: string
  fieldLabel: string
  oldValue?: string
  newValue?: string
}

export interface DeviceMigrationLog {
  id: number
  deviceId: number
  fromProjectIds?: string
  toProjectIds?: string
  fromProjectNames?: string
  toProjectNames?: string
  reason: string
  remark?: string
  operatorId?: number
  operatorName?: string
  migrateTime?: string
  items?: DeviceMigrationItem[]
}

export interface DeviceMigrateResult {
  migrationId: number
  deviceId: number
  changedFieldCount: number
  items: DeviceMigrationItem[]
}

export const DEVICE_STATUS_OPTIONS: DeviceStatusOption[] = [
  { label: '采购中', value: 'PURCHASING', type: 'info' },
  { label: '库存', value: 'IN_STOCK', type: 'primary' },
  { label: '待上架', value: 'PENDING_ONLINE', type: 'warning' },
  { label: '在用', value: 'IN_USE', type: 'success' },
  { label: '维修中', value: 'MAINTAINING', type: 'warning' },
  { label: '停用', value: 'STOPPED', type: 'info' },
  { label: '下架', value: 'OFFLINE', type: 'warning' },
  { label: '报废', value: 'SCRAPPED', type: 'danger' }
]

/** 设备表单可选主状态（不含「维修中」筛选伪码；维修由工单维护标志） */
export const DEVICE_MAIN_STATUS_OPTIONS: DeviceStatusOption[] = DEVICE_STATUS_OPTIONS.filter(
  item => item.value !== 'MAINTAINING'
)

export const MGMT_LOGIN_METHOD_OPTIONS = ['SSH', 'HTTPS', 'HTTP', 'IPMI', 'iDRAC', 'iLO', 'RDP', 'Telnet']

export function getDeviceStatusLabel(code?: string) {
  return DEVICE_STATUS_OPTIONS.find(item => item.value === code)?.label || code || '-'
}

export function getDeviceStatusType(code?: string): DeviceStatusOption['type'] {
  return DEVICE_STATUS_OPTIONS.find(item => item.value === code)?.type || 'info'
}

/** 主状态 + 维修标志同时展示（方案 A） */
export function getDeviceDisplayStatusLabel(code?: string, maintainingFlag?: number) {
  const maintaining = maintainingFlag === 1
  if (!maintaining) {
    return getDeviceStatusLabel(code)
  }
  if (code === 'IN_USE') {
    return '维修中/在用'
  }
  if (code === 'STOPPED') {
    return '维修中/停用'
  }
  return `${getDeviceStatusLabel(code)}（维修中）`
}

export function getDeviceDisplayStatusType(code?: string, maintainingFlag?: number): DeviceStatusOption['type'] {
  if (maintainingFlag === 1) {
    if (code === 'IN_USE') return 'success'
    if (code === 'STOPPED') return 'info'
    return 'warning'
  }
  return getDeviceStatusType(code)
}
