import type {
  AuthResponse,
  CarQuery,
  CarRequest,
  CarResponse,
  CarSummaryResponse,
  FavoriteResponse,
  LoginRequest,
  MessageResponse,
  PageResponse,
  RegisterRequest,
  UserResponse,
  VinLookupResponse,
} from '../types'
import { http } from './client'

export const authApi = {
  login: (req: LoginRequest) => http.post<AuthResponse>('/api/auth/login', req),
  register: (req: RegisterRequest) => http.post<UserResponse>('/api/auth/register', req),
  me: () => http.get<UserResponse>('/api/auth/me'),
  verifyEmail: (token: string) => http.post<MessageResponse>('/api/auth/verify-email', { token }),
  resendVerification: (email: string) => http.post<MessageResponse>('/api/auth/resend-verification', { email }),
  forgotPassword: (email: string) => http.post<MessageResponse>('/api/auth/forgot-password', { email }),
  resetPassword: (token: string, nuovaPassword: string) =>
    http.post<MessageResponse>('/api/auth/reset-password', { token, nuovaPassword }),
}

/** Catalogo pubblico: solo annunci PUBBLICATI. */
export const carsApi = {
  cerca: (query: CarQuery) => http.get<PageResponse<CarSummaryResponse>>('/api/cars', { ...query }),
  dettaglio: (id: string) => http.get<CarResponse>(`/api/cars/${id}`),
}

/** Area admin (ruolo ADMIN). */
export const adminApi = {
  elenco: (query: CarQuery) => http.get<PageResponse<CarSummaryResponse>>('/api/admin/cars', { ...query }),
  dettaglio: (id: string) => http.get<CarResponse>(`/api/admin/cars/${id}`),
  crea: (req: CarRequest) => http.post<CarResponse>('/api/admin/cars', req),
  aggiorna: (id: string, req: CarRequest) => http.put<CarResponse>(`/api/admin/cars/${id}`, req),
  aggiornaPrezzo: (id: string, prezzo: number) => http.patch<CarResponse>(`/api/admin/cars/${id}/prezzo`, { prezzo }),
  pubblica: (id: string) => http.post<CarResponse>(`/api/admin/cars/${id}/pubblica`),
  bozza: (id: string) => http.post<CarResponse>(`/api/admin/cars/${id}/bozza`),
  cercaVin: (vin: string) => http.get<VinLookupResponse>(`/api/admin/vin/${encodeURIComponent(vin)}`),
}

/** Preferiti dell'utente loggato. */
export const favoritesApi = {
  elenco: () => http.get<FavoriteResponse[]>('/api/me/favorites'),
  aggiungi: (carId: string) => http.put<FavoriteResponse>(`/api/me/favorites/${carId}`),
  rimuovi: (carId: string) => http.delete<void>(`/api/me/favorites/${carId}`),
  impostaSoglia: (carId: string, sogliaPrezzo: number | null) =>
    http.patch<FavoriteResponse>(`/api/me/favorites/${carId}/soglia`, { sogliaPrezzo }),
}
