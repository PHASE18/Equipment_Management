import type { App, Directive } from 'vue'
import { useAuthStore } from '@/stores/auth'

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

export function setupPermissionDirective(app: App) {
  app.directive('permission', permissionDirective)
}
