import { useCallback, useEffect, useState } from 'react'
import { messaggioErrore } from '../api/client'

interface State<T> {
  key: string
  data?: T
  error?: string
}

/**
 * Carica dati da un'API e li ricarica quando cambia `key` (es. la query string dei filtri).
 * Durante un ricaricamento restituisce ancora i dati precedenti, così la pagina non "salta".
 */
export function useAsync<T>(fn: () => Promise<T>, key: string) {
  const [reloads, setReloads] = useState(0)
  const fullKey = `${key}#${reloads}`
  const [state, setState] = useState<State<T>>({ key: '' })

  useEffect(() => {
    let attivo = true
    fn().then(
      (data) => attivo && setState({ key: fullKey, data }),
      (err) => attivo && setState((s) => ({ key: fullKey, data: s.data, error: messaggioErrore(err) })),
    )
    return () => {
      attivo = false
    }
    // fn cambia a ogni render: il caricamento dipende solo dalla chiave
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [fullKey])

  const reload = useCallback(() => setReloads((n) => n + 1), [])

  /** Aggiornamento locale dopo una mutazione (es. prezzo modificato), senza ricaricare. */
  const setData = useCallback((updater: (prev: T) => T) => {
    setState((s) => (s.data === undefined ? s : { ...s, data: updater(s.data) }))
  }, [])

  const loading = state.key !== fullKey
  return {
    data: state.data,
    error: loading ? null : (state.error ?? null),
    loading,
    reload,
    setData,
  }
}
