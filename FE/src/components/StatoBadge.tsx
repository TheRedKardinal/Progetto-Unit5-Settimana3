import { Badge } from 'react-bootstrap'
import type { StatoAnnuncio } from '../types'
import { STATO_LABEL } from '../utils/format'

/** Pubblicato: pill nera piena. Bozza: pill a filo. */
export default function StatoBadge({ stato }: { stato: StatoAnnuncio }) {
  return stato === 'PUBBLICATO' ? (
    <Badge bg="dark">{STATO_LABEL[stato]}</Badge>
  ) : (
    <Badge className="badge-outline">{STATO_LABEL[stato]}</Badge>
  )
}
