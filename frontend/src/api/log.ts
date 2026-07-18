/** 登录日志、操作审计和设备状态日志接口封装。 */
import http from '@/api/http'
import type { PageQuery, PageResult } from '@/types/api'
import type { DeviceStatusLog, LoginLog, OperationLog } from '@/types/log'

export interface LogQuery extends PageQuery {
  username?: string
  operationType?: string
  tableName?: string
  deviceId?: number
}

export function pageLoginLogsApi(params: LogQuery) {
  return http.get<PageResult<LoginLog>, PageResult<LoginLog>>('/log/login', { params })
}

export function pageOperationLogsApi(params: LogQuery) {
  return http.get<PageResult<OperationLog>, PageResult<OperationLog>>('/log/operation', { params })
}

export function pageStatusLogsApi(params: LogQuery) {
  return http.get<PageResult<DeviceStatusLog>, PageResult<DeviceStatusLog>>('/log/status', { params })
}

export function pageLoginLogRawApi(params: LogQuery) {
  return http.get<PageResult<LoginLog>, PageResult<LoginLog>>('/login-log/page', { params })
}

export function pageOperationLogRawApi(params: LogQuery) {
  return http.get<PageResult<OperationLog>, PageResult<OperationLog>>('/operation-log/page', { params })
}

export function pageStatusLogRawApi(params: LogQuery) {
  return http.get<PageResult<DeviceStatusLog>, PageResult<DeviceStatusLog>>('/device-status-log/page', {
    params
  })
}

export const logApi = {
  login: pageLoginLogsApi,
  operation: pageOperationLogsApi,
  status: pageStatusLogsApi
}
