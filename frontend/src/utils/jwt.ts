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

export function shouldRefreshToken(token: string, leadTimeMs = 5 * 60 * 1000): boolean {
  const expireAt = getTokenExpireAt(token)
  if (!expireAt) {
    return false
  }
  return Date.now() + leadTimeMs >= expireAt
}
