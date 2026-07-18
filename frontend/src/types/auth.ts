/** 登录接口提交的账号凭证。 */
export interface LoginRequest {
  username: string
  password: string
}

export interface UserInfo {
  id: number
  username: string
  name: string
  departmentId?: number
  department?: string
  dataScope?: 'ALL' | 'DEPARTMENT' | 'SELF'
  roles?: string[]
  permissions?: string[]
  menus?: MenuItem[]
}

export interface LoginResponse {
  token: string
  user: UserInfo
}

export interface MenuItem {
  id: number | string
  parentId?: number | string
  title: string
  path: string
  permissionCode: string
  icon?: string
  children?: MenuItem[]
}
