/** 文件上传、下载及设备附件管理接口封装。 */
import http from '@/api/http'
import axios from 'axios'
import type { FileCategory, FileMeta } from '@/types/file'
import { getToken } from '@/utils/token'

export function uploadFileApi(
  file: File,
  params: {
    deviceId: number
    category: FileCategory
    fileTypeCode?: string
    maintenanceId?: number
  }
) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('deviceId', String(params.deviceId))
  formData.append('category', params.category)
  if (params.fileTypeCode) {
    formData.append('fileTypeCode', params.fileTypeCode)
  }
  if (params.maintenanceId) {
    formData.append('maintenanceId', String(params.maintenanceId))
  }
  return http.post<FileMeta, FileMeta>('/file/upload', formData, {
    timeout: 120000
  })
}

export function listDeviceFilesApi(deviceId: number) {
  return http.get<FileMeta[], FileMeta[]>(`/file/list/${deviceId}`)
}

export function listMaintenanceFilesApi(maintenanceId: number) {
  return http.get<FileMeta[], FileMeta[]>(`/file/list/maintenance/${maintenanceId}`)
}

export function deleteFileApi(fileId: number) {
  return http.delete<void, void>(`/file/${fileId}`)
}

export function getFileDownloadUrl(fileId: number) {
  return `/api/file/download/${fileId}`
}

export async function previewFileApi(fileId: number) {
  const token = getToken()
  const response = await axios.get(`/api/file/download/${fileId}`, {
    responseType: 'blob',
    headers: token ? { Authorization: `Bearer ${token}` } : undefined
  })
  return URL.createObjectURL(response.data)
}
