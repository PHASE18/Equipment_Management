import axios, { type AxiosError, type InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import type { ApiResult } from '@/types/api'
import type { LoginResponse } from '@/types/auth'
import { getToken, removeToken, setToken } from '@/utils/token'

const http = axios.create({
  baseURL: '/api',
  timeout: 15000
})

let refreshingPromise: Promise<string> | null = null

http.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  response => {
    const result = response.data as ApiResult<unknown>
    if (typeof result?.code === 'number' && result.code !== 200) {
      return handleBusinessError(result.code, result.message, response.config)
    }
    return result?.data ?? response.data
  },
  (error: AxiosError<ApiResult<unknown>>) => {
    const status = error.response?.status
    const code = error.response?.data?.code
    const message = error.response?.data?.message || error.message || '请求失败'
    return handleBusinessError(code || status || 500, message, error.config)
  }
)

async function handleBusinessError(code: number, message: string, config?: InternalAxiosRequestConfig) {
  if (code === 1001 && config && !config.url?.includes('/refresh')) {
    try {
      const token = await refreshAccessToken()
      config.headers = config.headers || {}
      config.headers.Authorization = `Bearer ${token}`
      return http.request(config)
    } catch {
      forceLogout(message || '登录已失效')
      return Promise.reject(new Error(message || '登录已失效'))
    }
  }

  if (code === 401 || code === 1001) {
    forceLogout(message || '登录已失效')
    return Promise.reject(new Error(message || '登录已失效'))
  }

  if (code === 403 || code === 1003) {
    ElMessage.error(message || '无权限访问')
    return Promise.reject(new Error(message || '无权限访问'))
  }

  if (code === 1016) {
    ElMessage.error(message || '账号已锁定')
    return Promise.reject(new Error(message || '账号已锁定'))
  }

  ElMessage.error(message || '系统异常')
  return Promise.reject(new Error(message || '系统异常'))
}

async function refreshAccessToken() {
  if (!refreshingPromise) {
    refreshingPromise = axios
      .post<ApiResult<LoginResponse>>('/api/refresh', null, {
        headers: {
          Authorization: `Bearer ${getToken() || ''}`
        }
      })
      .then(response => {
        const result = response.data
        if (result.code !== 200 || !result.data?.token) {
          throw new Error(result.message || 'Token 续期失败')
        }
        setToken(result.data.token)
        return result.data.token
      })
      .finally(() => {
        refreshingPromise = null
      })
  }
  return refreshingPromise
}

function forceLogout(message: string) {
  removeToken()
  ElMessage.error(message)
  router.replace('/login')
}

export default http
