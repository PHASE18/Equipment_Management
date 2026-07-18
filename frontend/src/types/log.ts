/** 登录日志记录。 */
export interface LoginLog {
  id?: number
  username?: string
  loginIp?: string
  browser?: string
  loginTime?: string
  result?: number
}

export interface OperationLog {
  id?: number
  operatorId?: number
  operationType?: string
  tableName?: string
  businessId?: number
  beforeJson?: string
  afterJson?: string
  ip?: string
  browser?: string
  createTime?: string
}

export interface DeviceStatusLog {
  id?: number
  deviceId?: number
  oldStatusCode?: string
  newStatusCode?: string
  changeReason?: string
  remark?: string
  operatorId?: number
  changeTime?: string
}
