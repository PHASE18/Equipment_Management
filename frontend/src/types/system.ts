/** 系统管理实体共享的主键和审计字段。 */
export interface BaseEntity {
  id?: number
  createTime?: string
  updateTime?: string
}

export interface SysUser extends BaseEntity {
  username: string
  password?: string
  realName?: string
  departmentId?: number
  phone?: string
  email?: string
  status?: number
}

export interface SysRole extends BaseEntity {
  roleName: string
  roleCode: string
  remark?: string
}

export interface SysDepartment extends BaseEntity {
  departmentName: string
  parentId?: number
  leader?: string
  phone?: string
  remark?: string
}

export interface SysDict extends BaseEntity {
  dictType?: string
  dictCode: string
  dictName: string
  sort?: number
  status?: number
}

export interface UserQuery {
  pageNum: number
  pageSize: number
  keyword?: string
  departmentId?: number
}

export interface DepartmentTreeNode extends SysDepartment {
  label: string
  value: number
  children?: DepartmentTreeNode[]
}
