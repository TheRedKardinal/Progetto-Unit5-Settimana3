import { useEffect, useState } from 'react'
import { Badge, Button, Container } from 'react-bootstrap'
import { Link } from 'react-router-dom'
import { favoritesApi } from '../api'
import { messaggioErrore } from '../api/client'
import CarImage from '../components/CarImage'
import { EmptyState, ErrorAlert, Loader, PageHeader } from '../components/Feedback'
import PrezzoModal from '../components/PrezzoModal'
import { useFavorites } from '../context/useFavorites'
import type { FavoriteResponse } from '../types'
import { CARBURANTE_LABEL, CONDIZIONE_LABEL, formatData, formatKm, formatPrezzo } from '../utils/format'
import { useAsync } from '../utils/useAsync'

export default function Preferiti() {
  const { sync } = useFavorites()
  const { data, error, loading, reload, setData } = useAsync(favoritesApi.elenco, 'preferiti')
  const [sogliaDi, setSogliaDi] = useState<FavoriteResponse | null>(null)
  const [erroreAzione, setErroreAzione] = useState<string | null>(null)

  // Allinea i cuori del catalogo con la lista appena caricata
  useEffect(() => {
    if (data) sync(data.map((f) => f.car.id))
  }, [data, sync])

  const sostituisci = (aggiornato: FavoriteResponse) =>
    setData((list) => list.map((f) => (f.car.id === aggiornato.car.id ? aggiornato : f)))

  const rimuovi = async (carId: string) => {
    setErroreAzione(null)
    try {
      await favoritesApi.rimuovi(carId)
      setData((list) => list.filter((f) => f.car.id !== carId))
    } catch (err) {
      setErroreAzione(messaggioErrore(err))
    }
  }

  return (
    <Container>
      <PageHeader eyebrow="La tua selezione" title="Preferiti">
        {data && data.length > 0 && (
          <p className="text-secondary mb-0">
            {data.length} {data.length === 1 ? 'vettura salvata' : 'vetture salvate'}
          </p>
        )}
      </PageHeader>

      <ErrorAlert message={error} onRetry={reload} />
      <ErrorAlert message={erroreAzione} />
      {loading && !data && <Loader />}

      {data && data.length === 0 && (
        <EmptyState
          title="Nessun preferito, per ora"
          text="Tocca il cuore su una vettura del catalogo per ritrovarla qui e ricevere un avviso quando il prezzo scende."
          action={
            <Link to="/" className="btn btn-primary">
              Esplora il catalogo
            </Link>
          }
        />
      )}

      {data && data.length > 0 && (
        <div className="d-flex flex-column gap-3">
          {data.map((f) => {
            const disponibile = f.car.statoAnnuncio === 'PUBBLICATO'
            return (
              <article key={f.car.id} className={`fav-item trim${disponibile ? '' : ' is-unavailable'}`}>
                <div className="fav-item-media">
                  <CarImage src={f.car.copertina} alt={f.car.titolo} />
                </div>

                <div className="fav-item-info">
                  <div className="d-flex flex-wrap gap-2 mb-2">
                    {!disponibile && <Badge className="badge-outline">Non più disponibile</Badge>}
                    {disponibile && f.prezzoSottoSoglia && <Badge bg="dark">Sotto soglia</Badge>}
                  </div>
                  <h2 className="h3 mb-1">
                    {disponibile ? <Link to={`/auto/${f.car.id}`}>{f.car.titolo}</Link> : f.car.titolo}
                  </h2>
                  <p className="text-secondary small mb-2">
                    {f.car.anno} · {CARBURANTE_LABEL[f.car.carburante]} · {CONDIZIONE_LABEL[f.car.condizione]} ·{' '}
                    {formatKm(f.car.chilometraggio)}
                  </p>
                  <p className="fav-item-price mb-0">{formatPrezzo(f.car.prezzo)}</p>
                </div>

                <div className="fav-item-actions">
                  <div className="fav-soglia">
                    <span className="eyebrow d-block mb-1">Soglia di avviso</span>
                    <span className="fs-5">{f.sogliaPrezzo != null ? formatPrezzo(f.sogliaPrezzo) : 'Nessuna'}</span>
                    {f.notificatoAt && (
                      <span className="d-block small text-secondary">Avvisato il {formatData(f.notificatoAt)}</span>
                    )}
                  </div>
                  <div className="d-flex flex-wrap gap-2">
                    {disponibile && (
                      <Button variant="outline-dark" size="sm" onClick={() => setSogliaDi(f)}>
                        {f.sogliaPrezzo != null ? 'Modifica soglia' : 'Imposta soglia'}
                      </Button>
                    )}
                    <Button variant="link" size="sm" className="text-secondary" onClick={() => rimuovi(f.car.id)}>
                      Rimuovi
                    </Button>
                  </div>
                </div>
              </article>
            )
          })}
        </div>
      )}

      {sogliaDi && (
        <PrezzoModal
          show
          title="Soglia di prezzo"
          label="Avvisami sotto"
          help="Ti invieremo un'email quando il prezzo scende sotto questa cifra."
          fieldName="sogliaPrezzo"
          initial={sogliaDi.sogliaPrezzo}
          onHide={() => setSogliaDi(null)}
          onSave={(v) => favoritesApi.impostaSoglia(sogliaDi.car.id, v).then(sostituisci)}
          onRemove={() => favoritesApi.impostaSoglia(sogliaDi.car.id, null).then(sostituisci)}
        >
          <p className="text-secondary mb-4">
            {sogliaDi.car.titolo} · oggi a <strong className="text-dark">{formatPrezzo(sogliaDi.car.prezzo)}</strong>
          </p>
        </PrezzoModal>
      )}
    </Container>
  )
}
