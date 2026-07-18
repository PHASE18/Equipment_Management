/** 后端统一响应结构，data 在成功时承载业务结果。 */
export interface ApiResult<T> {
  code: number
  message: string
  data: T
}

export interface PageResult<T> {
  records: T[]
  total: number
  pageNum: number
  pageSize: number
  pages: number
}

export interface PageQuery {
  pageNum: number
  pageSize: number
  sortField?: string
  sortOrder?: 'asc' | 'desc'
}
