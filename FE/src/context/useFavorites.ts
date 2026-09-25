import { createContext, useContext } from 'react'

export interface FavoritesState {
  /** id delle auto nei preferiti dell'utente loggato (vuoto per gli ospiti) */
  ids: ReadonlySet<string>
  isFavorite: (carId: string) => boolean
  /** Aggiunge o rimuove; restituisce il nuovo stato */
  toggle: (carId: string) => Promise<boolean>
  /** Allinea gli id dopo un caricamento completo della lista (pagina Preferiti) */
  sync: (carIds: string[]) => void
}

export const FavoritesContext = createContext<FavoritesState | null>(null)

export function useFavorites() {
  const ctx = useContext(FavoritesContext)
  if (!ctx) throw new Error('useFavorites va usato dentro FavoritesProvider')
  return ctx
}
