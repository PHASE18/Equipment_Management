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

/** 带鉴权下载附件（blob），避免新窗口无 Token 导致 401。 */
export async function downloadFileApi(fileId: number, fileName?: string) {
  const token = getToken()
  const response = await axios.get(`/api/file/download/${fileId}`, {
    responseType: 'blob',
    headers: token ? { Authorization: `Bearer ${token}` } : undefined
  })

  const contentType = String(response.headers['content-type'] || '')
  if (contentType.includes('application/json')) {
    const text = await (response.data as Blob).text()
    let message = '下载失败'
    try {
      const body = JSON.parse(text) as { message?: string }
      message = body.message || message
    } catch {
      message = text || message
    }
    throw new Error(message)
  }

  const probe = await (response.data as Blob).slice(0, 1).text()
  if (probe === '{' || probe === '[') {
    const text = await (response.data as Blob).text()
    let message = '下载失败'
    try {
      const body = JSON.parse(text) as { message?: string }
      message = body.message || message
    } catch {
      message = text || message
    }
    throw new Error(message)
  }

  const disposition = String(response.headers['content-disposition'] || '')
  const matched = disposition.match(/filename\*=UTF-8''([^;]+)|filename="?([^";]+)"?/i)
  const headerName = matched ? decodeURIComponent(matched[1] || matched[2] || '') : ''
  const finalName = fileName || headerName || `file_${fileId}`

  const blob = new Blob([response.data])
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = finalName
  link.click()
  URL.revokeObjectURL(url)
}
