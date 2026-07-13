import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { getToken } from '@/utils/token'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/login/LoginView.vue'),
      meta: { public: true }
    },
    {
      path: '/403',
      name: 'Forbidden',
      component: () => import('@/views/error/ForbiddenView.vue'),
      meta: { public: true, title: '无权限访问' }
    }
  ]
})

router.beforeEach(async to => {
  const authStore = useAuthStore()
  const token = getToken()

  if (to.meta.public) {
    if (to.path === '/login' && token) {
      const authStore = useAuthStore()
      if (!authStore.routesReady) {
        await authStore.loadCurrentUser()
      }
      return authStore.menus[0]?.path || '/403'
    }
    return true
  }

  if (!token) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }

  if (!authStore.routesReady) {
    await authStore.loadCurrentUser()
    return to.fullPath
  }

  const permissionCode = to.meta.permissionCode
  if (typeof permissionCode === 'string' && !authStore.hasPermission(permissionCode)) {
    return '/403'
  }

  return true
})

export default router
