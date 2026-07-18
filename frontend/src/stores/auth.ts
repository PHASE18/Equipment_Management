import { defineStore } from 'pinia'
import { currentUserApi, loginApi, logoutApi, refreshTokenApi } from '@/api/auth'
import type { LoginRequest, MenuItem, UserInfo } from '@/types/auth'
import { shouldRefreshToken, getTokenExpireAt } from '@/utils/jwt'
import { hasAllPermissions, hasAnyPermission, hasPermission, isAdmin } from '@/utils/permission'
import { normalizeMenus } from '@/utils/menu'
import { getToken, removeToken, setToken } from '@/utils/token'
import { resetDynamicRoutes, setupDynamicRoutes } from '@/router/dynamic'

interface AuthState {
  token: string
  user: UserInfo | null
  menus: MenuItem[]
  routesReady: boolean
}

let refreshTimer: ReturnType<typeof setTimeout> | null = null

/** 全局认证状态，统一管理登录生命周期、用户信息和动态菜单。 */
export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    token: getToken() || '',
    user: null,
    menus: [],
    routesReady: false
  }),
  getters: {
    permissions: state => state.user?.permissions ?? [],
    roles: state => state.user?.roles ?? [],
    dataScopeLabel: state => {
      switch (state.user?.dataScope) {
        case 'ALL':
          return '全部数据'
        case 'DEPARTMENT':
          return '本部门数据'
        case 'SELF':
          return '本人负责设备'
        default:
          return ''
      }
    },
    isAdmin: state => isAdmin(state.user?.roles)
  },
  actions: {
    hasPermission(code: string) {
      if (this.isAdmin) {
        return true
      }
      return hasPermission(this.permissions, code)
    },
    hasAnyPermission(...codes: string[]) {
      if (this.isAdmin) {
        return true
      }
      return hasAnyPermission(this.permissions, ...codes)
    },
    hasAllPermissions(...codes: string[]) {
      if (this.isAdmin) {
        return true
      }
      return hasAllPermissions(this.permissions, ...codes)
    },
    async login(payload: LoginRequest) {
      const result = await loginApi(payload)
      this.applyAuthResult(result)
    },
    async refreshToken() {
      const result = await refreshTokenApi()
      this.applyAuthResult(result)
    },
    async loadCurrentUser() {
      const user = await currentUserApi()
      this.user = user
      this.menus = normalizeMenus(user.menus)
      setupDynamicRoutes(this.menus)
      this.routesReady = true
      this.scheduleTokenRefresh()
    },
    async logout() {
      try {
        await logoutApi()
      } finally {
        this.clearAuthState()
      }
    },
    applyAuthResult(result: { token: string; user?: UserInfo }) {
      this.token = result.token
      setToken(result.token)
      if (result.user) {
        this.user = result.user
        this.menus = normalizeMenus(result.user.menus)
        setupDynamicRoutes(this.menus)
        this.routesReady = true
      }
      this.scheduleTokenRefresh()
    },
    scheduleTokenRefresh() {
      if (refreshTimer) {
        clearTimeout(refreshTimer)
        refreshTimer = null
      }

      const token = this.token || getToken()
      if (!token) {
        return
      }

      const expireAt = getTokenExpireAt(token)
      if (!expireAt) {
        return
      }

      const leadTimeMs = 5 * 60 * 1000
      const refreshAt = Math.max(Date.now() + 30_000, expireAt - leadTimeMs)
      refreshTimer = setTimeout(async () => {
        try {
          if (shouldRefreshToken(this.token || getToken() || '')) {
            await this.refreshToken()
          } else {
            this.scheduleTokenRefresh()
          }
        } catch {
          await this.logout()
        }
      }, refreshAt - Date.now())
    },
    clearAuthState() {
      if (refreshTimer) {
        clearTimeout(refreshTimer)
        refreshTimer = null
      }
      this.token = ''
      this.user = null
      this.menus = []
      this.routesReady = false
      removeToken()
      resetDynamicRoutes()
    }
  }
})
