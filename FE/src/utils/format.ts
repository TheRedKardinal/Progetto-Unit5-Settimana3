import type { Carburante, Condizione, StatoAnnuncio } from '../types'

const euro = new Intl.NumberFormat('it-IT', { style: 'currency', currency: 'EUR', maximumFractionDigits: 0 })
const euroDecimali = new Intl.NumberFormat('it-IT', { style: 'currency', currency: 'EUR' })
const numero = new Intl.NumberFormat('it-IT', { maximumFractionDigits: 1 })
const dataLunga = new Intl.DateTimeFormat('it-IT', { day: 'numeric', month: 'long', year: 'numeric' })
const dataBreve = new Intl.DateTimeFormat('it-IT', { day: '2-digit', month: '2-digit', year: 'numeric' })

export const formatPrezzo = (v: number | null | undefined) => {
  if (v == null) return '—'
  return Number.isInteger(Number(v)) ? euro.format(v) : euroDecimali.format(v)
}
export const formatKm = (v: number | null | undefined) => (v == null ? '—' : `${numero.format(v)} km`)
export const formatData = (v: string | null | undefined) => (v ? dataLunga.format(new Date(v)) : '—')
export const formatDataBreve = (v: string | null | undefined) => (v ? dataBreve.format(new Date(v)) : '—')

export const CARBURANTE_LABEL: Record<Carburante, string> = {
  BENZINA: 'Benzina',
  DIESEL: 'Diesel',
  GPL: 'GPL',
  METANO: 'Metano',
  IBRIDA: 'Ibrida',
  ELETTRICA: 'Elettrica',
}

export const CONDIZIONE_LABEL: Record<Condizione, string> = {
  NUOVO: 'Nuovo',
  KM_0: 'Km 0',
  USATO: 'Usato',
}

export const STATO_LABEL: Record<StatoAnnuncio, string> = {
  BOZZA: 'Bozza',
  PUBBLICATO: 'Pubblicato',
}

/** Converte il valore di un input numerico in number (o null se vuoto/non valido). */
export const toNumberOrNull = (v: string) => {
  if (v.trim() === '') return null
  const n = Number(v.replace(',', '.'))
  return Number.isFinite(n) ? n : null
}
