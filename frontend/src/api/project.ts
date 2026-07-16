import http from '@/api/http'
import type { PageQuery, PageResult } from '@/types/api'
import type { Project } from '@/types/device'

export function pageProjectsApi(params: PageQuery & { keyword?: string }) {
  return http.get<PageResult<Project>, PageResult<Project>>('/project/list', { params })
}
