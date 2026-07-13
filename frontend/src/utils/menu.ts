import type { MenuItem } from '@/types/auth'

export function normalizeMenus(menus?: MenuItem[]) {
  return menus ?? []
}
