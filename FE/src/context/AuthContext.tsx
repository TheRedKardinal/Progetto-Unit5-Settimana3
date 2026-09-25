import { useCallback, useEffect, useMemo, useState, type ReactNode } from 'react'
import { authApi } from '../api'
import { setUnauthorizedHandler, tokenStore } from '../api/client'
import type { LoginRequest, UserResponse } from '../types'
import { AuthContext, type AuthState } from './useAuth'

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserResponse | null>(null)
  const [loading, setLoading] = useState(() => tokenStore.get() !== null)

  const logout = useCallback(() => {
    tokenStore.set(null)
    setUser(null)
  }, [])

  useEffect(() => {
    setUnauthorizedHandler(logout)
    return () => setUnauthorizedHandler(null)
  }, [logout])

  // Ripristina la sessione: il token salvato è valido solo se /me risponde
  useEffect(() => {
    if (!tokenStore.get()) return
    authApi
      .me()
      .then(setUser)
      .catch(() => tokenStore.set(null))
      .finally(() => setLoading(false))
  }, [])

  const login = useCallback(async (req: LoginRequest) => {
    const res = await authApi.login(req)
    tokenStore.set(res.token)
    setUser(res.user)
    return res.user
  }, [])

  const value = useMemo<AuthState>(
    () => ({ user, loading, isAdmin: user?.ruoli.includes('ADMIN') ?? false, login, logout }),
    [user, loading, login, logout],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
