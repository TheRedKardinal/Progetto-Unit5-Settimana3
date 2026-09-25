const BASE_URL = (import.meta.env.VITE_API_URL ?? 'http://localhost:8080').replace(/\/$/, '')
const TOKEN_KEY = 'salone.token'

/** Errore API costruito dal ProblemDetail (RFC 9457) restituito dal backend. */
export class ApiError extends Error {
  status: number
  title: string
  fieldErrors: Record<string, string>

  constructor(status: number, title: string, detail: string, fieldErrors: Record<string, string> = {}) {
    super(detail)
    this.status = status
    this.title = title
    this.fieldErrors = fieldErrors
  }
}

let onUnauthorized: (() => void) | null = null

/** Registrato da AuthContext: un 401 su una richiesta autenticata vuol dire token scaduto, quindi logout. */
export function setUnauthorizedHandler(handler: (() => void) | null) {
  onUnauthorized = handler
}

export const tokenStore = {
  get: () => {
    try {
      return localStorage.getItem(TOKEN_KEY)
    } catch {
      return null
    }
  },
  set: (token: string | null) => {
    try {
      if (token) localStorage.setItem(TOKEN_KEY, token)
      else localStorage.removeItem(TOKEN_KEY)
    } catch {
      // Storage non disponibile (es. navigazione privata): la sessione resta solo in memoria
    }
  },
}

type Query = Record<string, string | number | undefined | null>

function toQueryString(query?: Query) {
  if (!query) return ''
  const params = new URLSearchParams()
  for (const [key, value] of Object.entries(query)) {
    if (value !== undefined && value !== null && value !== '') params.set(key, String(value))
  }
  const s = params.toString()
  return s ? `?${s}` : ''
}

async function request<T>(method: string, path: string, options: { body?: unknown; query?: Query } = {}): Promise<T> {
  const token = tokenStore.get()
  const headers: Record<string, string> = { Accept: 'application/json' }
  if (options.body !== undefined) headers['Content-Type'] = 'application/json'
  if (token) headers.Authorization = `Bearer ${token}`

  let res: Response
  try {
    res = await fetch(`${BASE_URL}${path}${toQueryString(options.query)}`, {
      method,
      headers,
      body: options.body !== undefined ? JSON.stringify(options.body) : undefined,
    })
  } catch {
    throw new ApiError(0, 'Connessione non riuscita', 'Impossibile contattare il server. Riprova tra poco.')
  }

  if (res.status === 204) return undefined as T

  const data = await res.json().catch(() => null)
  if (!res.ok) {
    if (res.status === 401 && token) onUnauthorized?.()
    throw new ApiError(
      res.status,
      data?.title ?? 'Errore',
      data?.detail ?? 'Si è verificato un errore imprevisto',
      data?.errors ?? {},
    )
  }
  return data as T
}

export const http = {
  get: <T>(path: string, query?: Query) => request<T>('GET', path, { query }),
  post: <T>(path: string, body?: unknown) => request<T>('POST', path, { body }),
  put: <T>(path: string, body?: unknown) => request<T>('PUT', path, { body }),
  patch: <T>(path: string, body?: unknown) => request<T>('PATCH', path, { body }),
  delete: <T>(path: string) => request<T>('DELETE', path),
}

/** Messaggio leggibile per qualsiasi errore catturato. */
export function messaggioErrore(err: unknown) {
  if (err instanceof ApiError) return err.message
  return 'Si è verificato un errore imprevisto'
}
