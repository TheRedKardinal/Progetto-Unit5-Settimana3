import { useSearchParams } from 'react-router-dom'
import type { CarQuery } from '../types'
import type { ValoriFiltri } from '../components/FiltriAuto'

/**
 * Filtri, ordinamento e pagina salvati nella query string: la ricerca è condivisibile
 * e il tasto indietro del browser funziona.
 */
export function useFiltriUrl(sortDefault: string) {
  const [params, setParams] = useSearchParams()

  const valori: ValoriFiltri = {
    q: params.get('q') ?? '',
    carburante: params.get('carburante') ?? '',
    condizione: params.get('condizione') ?? '',
    prezzoMin: params.get('prezzoMin') ?? '',
    prezzoMax: params.get('prezzoMax') ?? '',
    stato: params.get('stato') ?? '',
    sort: params.get('sort') ?? sortDefault,
  }
  const page = Math.max(0, Number(params.get('page') ?? 0) || 0)

  const query: CarQuery = { ...valori, page }

  /** Cambiare un filtro riporta sempre alla prima pagina. */
  const applica = (patch: Partial<ValoriFiltri>) => {
    const next = new URLSearchParams(params)
    for (const [k, v] of Object.entries(patch)) {
      if (v && !(k === 'sort' && v === sortDefault)) next.set(k, v)
      else next.delete(k)
    }
    next.delete('page')
    setParams(next)
  }

  const vaiAPagina = (p: number) => {
    const next = new URLSearchParams(params)
    if (p > 0) next.set('page', String(p))
    else next.delete('page')
    setParams(next)
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  return { valori, page, query, key: params.toString(), applica, vaiAPagina }
}
