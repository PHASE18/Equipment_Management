/** 业务页只读选项：部门 / 品牌 / 类型 / 用户，登录即可，不依赖 system:* 管理权限。 */
import http from '@/api/http'
import type { SysDepartment, SysDict } from '@/types/system'

export interface UserOption {
  id: number
  username: string
  realName?: string
  departmentId?: number
  status?: number
}

export const optionsApi = {
  departments() {
    return http.get<SysDepartment[], SysDepartment[]>('/options/departments')
  },
  brands() {
    return http.get<SysDict[], SysDict[]>('/options/brands')
  },
  deviceTypes() {
    return http.get<SysDict[], SysDict[]>('/options/device-types')
  },
  users() {
    return http.get<UserOption[], UserOption[]>('/options/users')
  }
}
