import { createContext, useContext } from 'react'
import type { LoginRequest, UserResponse } from '../types'

export interface AuthState {
  user: UserResponse | null
  /** true finché non abbiamo verificato il token salvato con /api/auth/me */
  loading: boolean
  isAdmin: boolean
  login: (req: LoginRequest) => Promise<UserResponse>
  logout: () => void
}

export const AuthContext = createContext<AuthState | null>(null)

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth va usato dentro AuthProvider')
  return ctx
}
