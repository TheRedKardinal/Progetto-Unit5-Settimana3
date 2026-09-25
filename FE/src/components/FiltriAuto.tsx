import { useState, type FormEvent } from 'react'
import { Button, Col, Form, Row } from 'react-bootstrap'
import { CARBURANTI, CONDIZIONI } from '../types'
import { CARBURANTE_LABEL, CONDIZIONE_LABEL } from '../utils/format'
import Icon from './Icon'

export interface OpzioneOrdinamento {
  value: string
  label: string
}

/** Valori dei filtri così come stanno nella query string (stringa vuota = filtro assente). */
export interface ValoriFiltri {
  q: string
  carburante: string
  condizione: string
  prezzoMin: string
  prezzoMax: string
  sort: string
  stato: string
}

/**
 * Barra filtri di GET /api/cars e /api/admin/cars.
 * Le select si applicano subito; testo e prezzi con "Cerca" (o Invio), per non fare una richiesta a ogni tasto.
 * Va montata con key = query string corrente, così si riallinea quando l'URL cambia (es. tasto indietro).
 */
export default function FiltriAuto({
  valori,
  ordinamenti,
  conStato = false,
  onApply,
}: {
  valori: ValoriFiltri
  ordinamenti: OpzioneOrdinamento[]
  conStato?: boolean
  onApply: (patch: Partial<ValoriFiltri>) => void
}) {
  const [q, setQ] = useState(valori.q)
  const [prezzoMin, setPrezzoMin] = useState(valori.prezzoMin)
  const [prezzoMax, setPrezzoMax] = useState(valori.prezzoMax)

  const onSubmit = (e: FormEvent) => {
    e.preventDefault()
    onApply({ q: q.trim(), prezzoMin, prezzoMax })
  }

  const attivi = [valori.q, valori.carburante, valori.condizione, valori.prezzoMin, valori.prezzoMax, valori.stato].some(Boolean)

  return (
    <Form className="filter-bar surface" onSubmit={onSubmit}>
      <Row className="g-3 align-items-end">
        <Col xs={12} lg={conStato ? 3 : 4}>
          <Form.Label htmlFor="filtro-q">Cerca</Form.Label>
          <div className="input-icon">
            <Icon name="search" size={18} />
            <Form.Control
              id="filtro-q"
              type="search"
              placeholder="Marca, modello o titolo"
              value={q}
              onChange={(e) => setQ(e.target.value)}
            />
          </div>
        </Col>
        {conStato && (
          <Col xs={6} lg={2}>
            <Form.Label htmlFor="filtro-stato">Stato</Form.Label>
            <Form.Select id="filtro-stato" value={valori.stato} onChange={(e) => onApply({ stato: e.target.value })}>
              <option value="">Tutti</option>
              <option value="PUBBLICATO">Pubblicati</option>
              <option value="BOZZA">Bozze</option>
            </Form.Select>
          </Col>
        )}
        <Col xs={6} lg={conStato ? 2 : 2}>
          <Form.Label htmlFor="filtro-carburante">Carburante</Form.Label>
          <Form.Select
            id="filtro-carburante"
            value={valori.carburante}
            onChange={(e) => onApply({ carburante: e.target.value })}
          >
            <option value="">Tutti</option>
            {CARBURANTI.map((c) => (
              <option key={c} value={c}>
                {CARBURANTE_LABEL[c]}
              </option>
            ))}
          </Form.Select>
        </Col>
        <Col xs={6} lg={conStato ? 1 : 2}>
          <Form.Label htmlFor="filtro-condizione">Condizione</Form.Label>
          <Form.Select
            id="filtro-condizione"
            value={valori.condizione}
            onChange={(e) => onApply({ condizione: e.target.value })}
          >
            <option value="">Tutte</option>
            {CONDIZIONI.map((c) => (
              <option key={c} value={c}>
                {CONDIZIONE_LABEL[c]}
              </option>
            ))}
          </Form.Select>
        </Col>
        <Col xs={6} lg={2}>
          <Form.Label htmlFor="filtro-min">Prezzo (€)</Form.Label>
          <div className="d-flex gap-2">
            <Form.Control
              id="filtro-min"
              type="number"
              min={0}
              inputMode="numeric"
              placeholder="Min"
              value={prezzoMin}
              onChange={(e) => setPrezzoMin(e.target.value)}
              aria-label="Prezzo minimo"
            />
            <Form.Control
              type="number"
              min={0}
              inputMode="numeric"
              placeholder="Max"
              value={prezzoMax}
              onChange={(e) => setPrezzoMax(e.target.value)}
              aria-label="Prezzo massimo"
            />
          </div>
        </Col>
        <Col xs={12} lg={2}>
          <Form.Label htmlFor="filtro-sort">Ordina per</Form.Label>
          <Form.Select id="filtro-sort" value={valori.sort} onChange={(e) => onApply({ sort: e.target.value })}>
            {ordinamenti.map((o) => (
              <option key={o.value} value={o.value}>
                {o.label}
              </option>
            ))}
          </Form.Select>
        </Col>
      </Row>
      <div className="d-flex flex-wrap gap-2 justify-content-end mt-3">
        {attivi && (
          <Button
            variant="link"
            className="text-secondary text-decoration-none"
            onClick={() => onApply({ q: '', carburante: '', condizione: '', prezzoMin: '', prezzoMax: '', stato: '' })}
          >
            Azzera filtri
          </Button>
        )}
        <Button type="submit">Cerca</Button>
      </div>
    </Form>
  )
}
