/** 判断权限集合是否包含指定权限；空权限编码表示无需校验。 */
export function hasPermission(permissions: string[] | undefined, code: string) {
  if (!code) {
    return true
  }
  if (!permissions || permissions.length === 0) {
    return false
  }
  if (permissions.includes('*') || permissions.includes('ADMIN')) {
    return true
  }
  return permissions.includes(code)
}

/** 判断权限集合是否至少包含一个目标权限。 */
export function hasAnyPermission(permissions: string[] | undefined, ...codes: string[]) {
  return codes.some(code => hasPermission(permissions, code))
}

/** 判断权限集合是否同时包含所有目标权限。 */
export function hasAllPermissions(permissions: string[] | undefined, ...codes: string[]) {
  return codes.every(code => hasPermission(permissions, code))
}

export function isAdmin(roles: string[] | undefined) {
  if (!roles || roles.length === 0) {
    return false
  }
  return roles.some(role => ['ADMIN', 'SUPER_ADMIN', 'SYS_ADMIN', 'SYSTEM_ADMIN'].includes(role))
}
