import { Link } from 'react-router-dom'
import type { CarSummaryResponse } from '../types'
import { CARBURANTE_LABEL, CONDIZIONE_LABEL, formatKm, formatPrezzo } from '../utils/format'
import CarImage from './CarImage'
import FavoriteButton from './FavoriteButton'

export default function CarCard({ car }: { car: CarSummaryResponse }) {
  return (
    <Link to={`/auto/${car.id}`} className="car-card">
      <div className="car-card-media">
        <CarImage src={car.copertina} alt={car.titolo} />
        <div className="car-card-fav">
          <FavoriteButton carId={car.id} />
        </div>
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
