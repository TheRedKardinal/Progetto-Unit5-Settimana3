import type { PointerEvent } from 'react'
import { Link } from 'react-router-dom'
import type { CarSummaryResponse } from '../types'
import { CARBURANTE_LABEL, CONDIZIONE_LABEL, formatKm, formatPrezzo } from '../utils/format'
import CarImage from './CarImage'
import FavoriteButton from './FavoriteButton'
import Icon from './Icon'

const INCLINAZIONE_MAX = 3 // gradi

const movimentoRidotto = () => window.matchMedia('(prefers-reduced-motion: reduce)').matches

/**
 * Inclinazione e riflesso che seguono il mouse: scrive variabili CSS direttamente sull'elemento,
 * senza stato React (nessun re-render a ogni movimento). Niente effetto su touch o con "riduci movimento".
 */
function segui(e: PointerEvent<HTMLElement>) {
  if (e.pointerType !== 'mouse' || movimentoRidotto()) return
  const el = e.currentTarget
  const r = el.getBoundingClientRect()
  const x = (e.clientX - r.left) / r.width
  const y = (e.clientY - r.top) / r.height
  el.style.setProperty('--mx', `${(x * 100).toFixed(1)}%`)
  el.style.setProperty('--my', `${(y * 100).toFixed(1)}%`)
  el.style.setProperty('--ry', `${((x - 0.5) * 2 * INCLINAZIONE_MAX).toFixed(2)}deg`)
  el.style.setProperty('--rx', `${((0.5 - y) * 2 * INCLINAZIONE_MAX).toFixed(2)}deg`)
}

function riposo(e: PointerEvent<HTMLElement>) {
  const el = e.currentTarget
  el.style.removeProperty('--rx')
  el.style.removeProperty('--ry')
}

export default function CarCard({ car }: { car: CarSummaryResponse }) {
  return (
    <Link to={`/auto/${car.id}`} className="car-card trim" onPointerMove={segui} onPointerLeave={riposo}>
      <div className="car-card-media">
        <CarImage src={car.copertina} alt={car.titolo} />
        <div className="car-card-fav">
          <FavoriteButton carId={car.id} />
        </div>
        <span className="car-card-cta" aria-hidden="true">
          Scopri <Icon name="arrowRight" size={16} />
        </span>
      </div>
      <div className="car-card-body">
        <p className="eyebrow mb-2">
          {car.anno} · {CARBURANTE_LABEL[car.carburante]} · {CONDIZIONE_LABEL[car.condizione]}
        </p>
        <h3 className="car-card-title">{car.titolo}</h3>
        <p className="text-secondary small mb-4">
          {car.marca} {car.modello}
        </p>
        <div className="car-card-footer">
          <span className="text-secondary small">{formatKm(car.chilometraggio)}</span>
          <span className="car-card-price">{formatPrezzo(car.prezzo)}</span>
        </div>
      </div>
    </Link>
  )
}
