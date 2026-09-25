import { Col, Container, Row } from 'react-bootstrap'
import { Link, useParams } from 'react-router-dom'
import { carsApi } from '../api'
import FavoriteButton from '../components/FavoriteButton'
import { BackLink, ErrorAlert, Loader } from '../components/Feedback'
import Galleria from '../components/Galleria'
import { useAuth } from '../context/useAuth'
import { CARBURANTE_LABEL, CONDIZIONE_LABEL, formatData, formatKm, formatPrezzo } from '../utils/format'
import { useAsync } from '../utils/useAsync'

/** /auto/:id — anche destinazione del link nell'email di avviso prezzo. */
export default function Dettaglio() {
  const { id = '' } = useParams()
  const { user, isAdmin } = useAuth()
  const { data: car, error, loading, reload } = useAsync(() => carsApi.dettaglio(id), id)

  if (loading && !car) return <Loader />

  if (error && !car) {
    return (
      <Container className="section">
        <BackLink to="/">Catalogo</BackLink>
        <div className="mt-4">
          <ErrorAlert message={error} onRetry={reload} />
        </div>
      </Container>
    )
  }
  if (!car) return null

  return (
    <Container className="section">
      <BackLink to="/">Catalogo</BackLink>

      <Row className="g-4 g-xl-5 mt-1">
        <Col lg={7} xl={8}>
          <Galleria immagini={car.immagini} alt={car.titolo} />
        </Col>

        <Col lg={5} xl={4}>
          <aside className="detail-panel surface">
            <p className="eyebrow mb-2">{CONDIZIONE_LABEL[car.condizione]}</p>
            <h1 className="detail-title">{car.titolo}</h1>
            <p className="text-secondary mb-4">
              {car.marca} {car.modello} · {car.anno}
            </p>

            <p className="detail-price">{formatPrezzo(car.prezzo)}</p>

            <dl className="spec-grid mb-4">
              <div>
                <dt>Anno</dt>
                <dd>{car.anno}</dd>
              </div>
              <div>
                <dt>Chilometraggio</dt>
                <dd>{formatKm(car.chilometraggio)}</dd>
              </div>
              <div>
                <dt>Carburante</dt>
                <dd>{CARBURANTE_LABEL[car.carburante]}</dd>
              </div>
              <div>
                <dt>Condizione</dt>
                <dd>{CONDIZIONE_LABEL[car.condizione]}</dd>
              </div>
            </dl>

            {car.vin && (
              <p className="small text-secondary mb-4">
                <span className="eyebrow me-2">VIN</span>
                <span className="font-monospace">{car.vin}</span>
              </p>
            )}

            <FavoriteButton carId={car.id} variant="full" />
            <p className="small text-secondary mt-3 mb-0">
              {user ? (
                <>
                  Dai <Link to="/preferiti" className="link-underline">preferiti</Link> puoi impostare una soglia di
                  prezzo: ti avviseremo via email quando scende.
                </>
              ) : (
                'Accedi per salvarla nei preferiti e ricevere un avviso quando il prezzo scende.'
              )}
            </p>

            {isAdmin && (
              <Link to={`/admin/auto/${car.id}`} className="btn btn-outline-dark w-100 mt-4">
                Modifica annuncio
              </Link>
            )}
          </aside>
        </Col>
      </Row>

      <section className="detail-description">
        <Row className="g-4">
          <Col lg={4}>
            <p className="eyebrow mb-2">Descrizione</p>
            <p className="text-secondary small mb-0">Pubblicato il {formatData(car.publishedAt)}</p>
          </Col>
          <Col lg={8}>
            <div className="detail-text">{car.descrizione}</div>
          </Col>
        </Row>
      </section>
    </Container>
  )
}
