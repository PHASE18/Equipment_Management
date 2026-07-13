import http from '@/api/http'
import type { LoginRequest, LoginResponse, UserInfo } from '@/types/auth'

export function loginApi(data: LoginRequest) {
  return http.post<LoginResponse, LoginResponse>('/login', data)
}

export function refreshTokenApi() {
  return http.post<LoginResponse, LoginResponse>('/refresh')
}

export function currentUserApi() {
  return http.get<UserInfo, UserInfo>('/user/info')
}

export function logoutApi() {
  return http.post<void, void>('/logout')
}

export function changePasswordApi(data: { oldPassword: string; newPassword: string }) {
  return http.put<void, void>('/user/password', data)
}
