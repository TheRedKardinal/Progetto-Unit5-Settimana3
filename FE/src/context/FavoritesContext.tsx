import { useCallback, useEffect, useMemo, useState, type ReactNode } from 'react'
import { favoritesApi } from '../api'
import { FavoritesContext, type FavoritesState } from './useFavorites'
import { useAuth } from './useAuth'

export function FavoritesProvider({ children }: { children: ReactNode }) {
  const { user } = useAuth()
  const [ids, setIds] = useState<ReadonlySet<string>>(new Set())
  const [ownerId, setOwnerId] = useState<string | null>(null)

  // Carica gli id quando cambia l'utente loggato
  useEffect(() => {
    if (!user) return
    let attivo = true
    favoritesApi
      .elenco()
      .then((list) => {
        if (!attivo) return
        setIds(new Set(list.map((f) => f.car.id)))
        setOwnerId(user.id)
      })
      .catch(() => {
        // Non bloccante: il cuore resta vuoto, la pagina Preferiti mostrerà l'errore
      })
    return () => {
      attivo = false
    }
  }, [user])

  // Dopo il logout (o prima del primo caricamento) nessun preferito, senza setState nell'effetto
  const visibili = useMemo(() => (user && ownerId === user.id ? ids : new Set<string>()), [user, ownerId, ids])

  const toggle = useCallback(
    async (carId: string) => {
      const presente = ids.has(carId)
      if (presente) await favoritesApi.rimuovi(carId)
      else await favoritesApi.aggiungi(carId)
      setIds((prev) => {
        const next = new Set(prev)
        if (presente) next.delete(carId)
        else next.add(carId)
        return next
      })
      return !presente
    },
    [ids],
  )

  const sync = useCallback((carIds: string[]) => setIds(new Set(carIds)), [])

  const value = useMemo<FavoritesState>(
    () => ({ ids: visibili, isFavorite: (id) => visibili.has(id), toggle, sync }),
    [visibili, toggle, sync],
  )

  return <FavoritesContext.Provider value={value}>{children}</FavoritesContext.Provider>
}
