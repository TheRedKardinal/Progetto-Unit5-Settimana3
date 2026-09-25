import { Link } from 'react-router-dom'
import type { CarSummaryResponse } from '../types'
import { CARBURANTE_LABEL, CONDIZIONE_LABEL, formatKm, formatPrezzo } from '../utils/format'
import CarImage from './CarImage'
import FavoriteButton from './FavoriteButton'
import Icon from './Icon'

/** Pannello "In evidenza": una vettura in grande, su fondo scuro, per spezzare il ritmo della griglia. */
export function InEvidenza({ car }: { car: CarSummaryResponse }) {
  return (
    <section className="spotlight trim" aria-labelledby="spotlight-titolo">
      <Link to={`/auto/${car.id}`} className="spotlight-media" tabIndex={-1} aria-hidden="true">
        <CarImage src={car.copertina} alt="" />
      </Link>
      <div className="spotlight-body">
        <p className="eyebrow spotlight-eyebrow mb-3">In evidenza</p>
        <h2 id="spotlight-titolo" className="spotlight-title">
          <Link to={`/auto/${car.id}`}>{car.titolo}</Link>
        </h2>
        <p className="spotlight-sub">
          {car.marca} {car.modello} · {car.anno}
        </p>
        <ul className="spotlight-specs">
          <li>{formatKm(car.chilometraggio)}</li>
          <li>{CARBURANTE_LABEL[car.carburante]}</li>
          <li>{CONDIZIONE_LABEL[car.condizione]}</li>
        </ul>
        <div className="spotlight-footer">
          <span className="spotlight-price">{formatPrezzo(car.prezzo)}</span>
          <div className="d-flex align-items-center gap-2">
            <FavoriteButton carId={car.id} />
            <Link to={`/auto/${car.id}`} className="btn btn-on-deep d-inline-flex align-items-center gap-2">
              Scopri <Icon name="arrowRight" size={18} />
            </Link>
          </div>
        </div>
      </div>
    </section>
  )
}

/**
 * Nastro delle marche in scorrimento continuo (si ferma al passaggio del mouse); un clic filtra il catalogo.
 * L'elenco è duplicato per chiudere il giro senza stacchi; la copia è nascosta agli screen reader.
 */
export function NastroMarche({ marche, onSelect }: { marche: string[]; onSelect: (marca: string) => void }) {
  if (marche.length === 0) return null
  const voci = (copia: boolean) =>
    marche.map((m) => (
      <li key={`${copia ? 'c' : 'o'}-${m}`} aria-hidden={copia || undefined}>
        <button type="button" onClick={() => onSelect(m)} tabIndex={copia ? -1 : undefined}>
          {m}
        </button>
      </li>
    ))
  return (
    <nav className="brand-strip" aria-label="Marche della collezione">
      <p className="eyebrow mb-0 brand-strip-label">Le marche</p>
      <div className="brand-strip-viewport">
        <ul className="brand-strip-track">
          {voci(false)}
          {voci(true)}
        </ul>
      </div>
    </nav>
  )
}
