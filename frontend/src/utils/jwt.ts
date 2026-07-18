/** 读取 JWT payload 中的过期时间；这里只解码，不负责校验签名。 */
export function getTokenExpireAt(token: string): number | null {
  try {
    const payloadPart = token.split('.')[1]
    if (!payloadPart) {
      return null
    }
    const payload = JSON.parse(atob(payloadPart.replace(/-/g, '+').replace(/_/g, '/')))
    return typeof payload.exp === 'number' ? payload.exp * 1000 : null
  } catch {
    return null
  }
}

/** 判断令牌是否进入前端自动刷新窗口。 */
export function shouldRefreshToken(token: string, leadTimeMs = 5 * 60 * 1000): boolean {
  const expireAt = getTokenExpireAt(token)
  if (!expireAt) {
    return false
  }
  return Date.now() + leadTimeMs >= expireAt
}
