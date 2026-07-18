/** 项目查询、维护和设备项目绑定接口封装。 */
import http from '@/api/http'
import type { PageQuery, PageResult } from '@/types/api'
import type { Project } from '@/types/device'

export function pageProjectsApi(params: PageQuery & { keyword?: string }) {
  return http.get<PageResult<Project>, PageResult<Project>>('/project/list', { params })
}

export function getProjectApi(id: number) {
  return http.get<Project, Project>(`/project/${id}`)
}

export function createProjectApi(data: Partial<Project>) {
  return http.post<void, void>('/project', data)
}

export function updateProjectApi(data: Partial<Project>) {
  return http.put<void, void>('/project', data)
}

export function deleteProjectApi(id: number) {
  return http.delete<void, void>(`/project/${id}`)
}

export function bindDeviceToProjectApi(projectId: number, deviceId: number) {
  return http.post<void, void>('/project/bindDevice', { projectId, deviceId })
}

export function unbindDeviceFromProjectApi(projectId: number, deviceId: number) {
  return http.delete<void, void>('/project/unbind', { data: { projectId, deviceId } })
}

export const projectApi = {
  page: pageProjectsApi,
  getById: getProjectApi,
  create: createProjectApi,
  update: updateProjectApi,
  remove: deleteProjectApi,
  bindDevice: bindDeviceToProjectApi,
  unbindDevice: unbindDeviceFromProjectApi
}
