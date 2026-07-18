import router from '@/router'
import AppLayout from '@/layouts/AppLayout.vue'
import type { MenuItem } from '@/types/auth'
import { routeRegistry } from '@/router/routeRegistry'

const dynamicRouteNames = new Set<string>()

/** 根据后端菜单权限动态注册布局及业务页面路由。 */
export function setupDynamicRoutes(menus: MenuItem[]) {
  const defaultPath = menus[0]?.path || '/403'

  if (!router.hasRoute('Layout')) {
    router.addRoute({
      path: '/',
      name: 'Layout',
      component: AppLayout,
      redirect: defaultPath,
      children: []
    })
  }

  flattenMenus(menus).forEach(menu => {
    const registry = routeRegistry[menu.permissionCode]
    if (!registry || router.hasRoute(registry.name)) {
      return
    }
    router.addRoute('Layout', {
      path: registry.path,
      name: registry.name,
      component: registry.component,
      meta: {
        title: menu.title || registry.title,
        permissionCode: menu.permissionCode
      }
    })
    dynamicRouteNames.add(registry.name)
  })

  // if (!router.hasRoute('NotFound')) {
  //   router.addRoute({
  //     path: '/:pathMatch(.*)*',
  //     name: 'NotFound',
  //     component: () => import('@/views/error/NotFoundView.vue')
  //   })
  // }
}

/** 清理上一次登录用户注册的动态路由，防止权限在用户间残留。 */
export function resetDynamicRoutes() {
  dynamicRouteNames.forEach(name => {
    if (router.hasRoute(name)) {
      router.removeRoute(name)
    }
  })
  dynamicRouteNames.clear()
  if (router.hasRoute('Layout')) {
    router.removeRoute('Layout')
  }
  if (router.hasRoute('NotFound')) {
    router.removeRoute('NotFound')
  }
}

/** 将树形菜单展开为便于逐项匹配路由注册表的一维数组。 */
function flattenMenus(menus: MenuItem[]) {
  const result: MenuItem[] = []
  menus.forEach(menu => {
    result.push(menu)
    if (menu.children?.length) {
      result.push(...flattenMenus(menu.children))
    }
  })
  return result
}
