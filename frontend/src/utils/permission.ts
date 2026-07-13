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

export function hasAnyPermission(permissions: string[] | undefined, ...codes: string[]) {
  return codes.some(code => hasPermission(permissions, code))
}

export function hasAllPermissions(permissions: string[] | undefined, ...codes: string[]) {
  return codes.every(code => hasPermission(permissions, code))
}

export function isAdmin(roles: string[] | undefined) {
  if (!roles || roles.length === 0) {
    return false
  }
  return roles.some(role => ['ADMIN', 'SUPER_ADMIN', 'SYS_ADMIN', 'SYSTEM_ADMIN'].includes(role))
}
