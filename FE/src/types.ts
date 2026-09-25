// Tipi 1:1 con i DTO del backend (BE/src/main/java/com/example/be/dto)

export type Carburante = 'BENZINA' | 'DIESEL' | 'GPL' | 'METANO' | 'IBRIDA' | 'ELETTRICA'
export type Condizione = 'NUOVO' | 'KM_0' | 'USATO'
export type StatoAnnuncio = 'BOZZA' | 'PUBBLICATO'

export const CARBURANTI: Carburante[] = ['BENZINA', 'DIESEL', 'GPL', 'METANO', 'IBRIDA', 'ELETTRICA']
export const CONDIZIONI: Condizione[] = ['NUOVO', 'KM_0', 'USATO']

export interface PageResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export interface MessageResponse {
  message: string
}

export interface UserResponse {
  id: string
  nome: string
  cognome: string
  username: string
  email: string
  ruoli: string[]
  createdAt: string
}

export interface AuthResponse {
  token: string
  expiresAt: string
  user: UserResponse
}

export interface LoginRequest {
  emailOrUsername: string
  password: string
}

export interface RegisterRequest {
  nome: string
  cognome: string
  username: string
  email: string
  password: string
}

export interface CarSummaryResponse {
  id: string
  marca: string
  modello: string
  anno: number
  titolo: string
  chilometraggio: number
  carburante: Carburante
  prezzo: number
  condizione: Condizione
  statoAnnuncio: StatoAnnuncio
  copertina: string | null
  publishedAt: string | null
  updatedAt: string
}

export interface CarResponse {
  id: string
  vin: string | null
  marca: string
  modello: string
  anno: number
  titolo: string
  descrizione: string
  chilometraggio: number
  carburante: Carburante
  prezzo: number
  condizione: Condizione
  statoAnnuncio: StatoAnnuncio
  immagini: string[]
  createdAt: string
  updatedAt: string
  publishedAt: string | null
}

/** Body di POST/PUT /api/admin/cars. I null vengono respinti dalla validazione del backend con messaggi per campo. */
export interface CarRequest {
  vin: string
  marca: string
  modello: string
  anno: number | null
  titolo: string
  descrizione: string
  chilometraggio: number | null
  carburante: Carburante | null
  prezzo: number | null
  condizione: Condizione | null
  immagini: string[]
}

export interface VinLookupResponse {
  vin: string
  marca: string | null
  modello: string | null
  anno: number | null
  motore: string | null
  carburante: Carburante | null
  immagini: string[]
}

export interface FavoriteResponse {
  car: CarSummaryResponse
  sogliaPrezzo: number | null
  prezzoSottoSoglia: boolean
  notificatoAt: string | null
  createdAt: string
}

/** Query string di GET /api/cars e /api/admin/cars (stato solo per l'admin). */
export interface CarQuery {
  q?: string
  carburante?: string
  condizione?: string
  prezzoMin?: string
  prezzoMax?: string
  stato?: string
  page?: number
  size?: number
  sort?: string
}
