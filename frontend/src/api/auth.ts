import http from '@/api/http'
import type { LoginRequest, LoginResponse, UserInfo } from '@/types/auth'

/** 调用登录接口并获取访问令牌及用户概要信息。 */
export function loginApi(data: LoginRequest) {
  return http.post<LoginResponse, LoginResponse>('/login', data)
}

/** 使用当前令牌申请刷新后的令牌。 */
export function refreshTokenApi() {
  return http.post<LoginResponse, LoginResponse>('/refresh')
}

/** 获取当前登录用户及其菜单、权限信息。 */
export function currentUserApi() {
  return http.get<UserInfo, UserInfo>('/user/info')
}

/** 通知后端注销当前会话。 */
export function logoutApi() {
  return http.post<void, void>('/logout')
}

/** 提交旧密码和新密码，修改当前用户密码。 */
export function changePasswordApi(data: { oldPassword: string; newPassword: string }) {
  return http.put<void, void>('/user/password', data)
}
