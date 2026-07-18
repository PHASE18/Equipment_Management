import type { App, Directive } from 'vue'
import { useAuthStore } from '@/stores/auth'

// 可以传入单个权限字符串或权限数组，返回当前用户是否拥有这些权限
function checkPermission(value?: string | string[]) { 
  const authStore = useAuthStore()
  if (!value) {
    return true
  }
  if (Array.isArray(value)) {
    return authStore.hasAnyPermission(...value)
  }
  return authStore.hasPermission(value)
}

/** 根据当前用户权限控制按钮或操作节点的显示。 */
export const permissionDirective: Directive<HTMLElement, string | string[]> = {
  mounted(el, binding) {
    if (!checkPermission(binding.value)) {
      el.parentNode?.removeChild(el)
    }
  },
  updated(el, binding) {
    if (!checkPermission(binding.value)) {
      el.style.display = 'none'
    } else {
      el.style.display = ''
    }
  }
}

/** 将权限指令注册到 Vue 应用实例。 */
export function setupPermissionDirective(app: App) {
  app.directive('permission', permissionDirective)
}
