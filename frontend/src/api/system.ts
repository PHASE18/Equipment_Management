/** 用户、角色、部门、字典和设备基础字典接口封装。 */
import http from '@/api/http'
import type { PageQuery, PageResult } from '@/types/api'
import type { SysDepartment, SysDict, SysRole, SysUser, UserQuery } from '@/types/system'

function createCrudApi<T>(basePath: string) {
  return {
    page(params: PageQuery & Record<string, unknown>) {
      return http.get<PageResult<T>, PageResult<T>>(`${basePath}/page`, { params })
    },
    getById(id: number) {
      return http.get<T, T>(`${basePath}/${id}`)
    },
    create(data: Partial<T>) {
      return http.post<void, void>(basePath, data)
    },
    update(data: Partial<T>) {
      return http.put<void, void>(basePath, data)
    },
    remove(id: number) {
      return http.delete<void, void>(`${basePath}/${id}`)
    }
  }
}

export const userApi = {
  ...createCrudApi<SysUser>('/user'),
  list(params: UserQuery) {
    return http.get<PageResult<SysUser>, PageResult<SysUser>>('/user/list', { params })
  },
  resetPassword(id: number) {
    return http.put<void, void>(`/user/resetPassword/${id}`)
  }
}

export const roleApi = createCrudApi<SysRole>('/role')

export const departmentApi = {
  ...createCrudApi<SysDepartment>('/department'),
  tree() {
    return http.get<SysDepartment[], SysDepartment[]>('/department/tree')
  }
}

export const deviceBrandApi = createCrudApi<SysDict>('/device-brand')
export const deviceTypeApi = createCrudApi<SysDict>('/device-type')

export function listUserRoleIdsApi(userId: number) {
  return http.get<number[], number[]>(`/user-role/by-user/${userId}`)
}

export function bindUserRolesApi(userId: number, roleIds: number[]) {
  return http.post<void, void>('/user-role/bind', { userId, roleIds })
}

export function unbindUserRolesApi(userId: number, roleIds: number[]) {
  return http.delete<void, void>('/user-role/unbind', { data: { userId, roleIds } })
}

export async function syncUserRolesApi(userId: number, nextRoleIds: number[]) {
  const currentRoleIds = await listUserRoleIdsApi(userId)
  const toAdd = nextRoleIds.filter(id => !currentRoleIds.includes(id))
  const toRemove = currentRoleIds.filter(id => !nextRoleIds.includes(id))
  if (toAdd.length) {
    await bindUserRolesApi(userId, toAdd)
  }
  if (toRemove.length) {
    await unbindUserRolesApi(userId, toRemove)
  }
}

export interface DepartmentTreeOption {
  label: string
  value: number
  children?: DepartmentTreeOption[]
}

export function buildDepartmentTreeOptions(departments: SysDepartment[]): DepartmentTreeOption[] {
  const map = new Map<number, SysDepartment & { children: SysDepartment[] }>()
  departments.forEach(item => {
    map.set(item.id!, { ...item, children: [] })
  })

  const roots: (SysDepartment & { children: SysDepartment[] })[] = []
  map.forEach(node => {
    const parentId = node.parentId ?? 0
    if (parentId === 0 || !map.has(parentId)) {
      roots.push(node)
      return
    }
    map.get(parentId)!.children.push(node)
  })

  const toOptions = (nodes: Array<SysDepartment & { children?: SysDepartment[] }>): DepartmentTreeOption[] =>
    nodes.map(node => ({
      label: node.departmentName,
      value: node.id!,
      children: node.children?.length ? toOptions(node.children as Array<SysDepartment & { children?: SysDepartment[] }>) : undefined
    }))

  return toOptions(roots)
}

export function findDepartmentName(departments: SysDepartment[], id?: number) {
  if (!id) {
    return ''
  }
  return departments.find(item => item.id === id)?.departmentName || ''
}
