import { useState } from 'react'
import { Button, Container, OverlayTrigger, Table, Tooltip } from 'react-bootstrap'
import { Link } from 'react-router-dom'
import { adminApi } from '../../api'
import { messaggioErrore } from '../../api/client'
import CarImage from '../../components/CarImage'
import { EmptyState, ErrorAlert, Loader, PageHeader } from '../../components/Feedback'
import FiltriAuto, { type OpzioneOrdinamento } from '../../components/FiltriAuto'
import Icon, { type IconName } from '../../components/Icon'
import Paginazione from '../../components/Paginazione'
import PrezzoModal from '../../components/PrezzoModal'
import StatoBadge from '../../components/StatoBadge'
import type { CarResponse, CarSummaryResponse, PageResponse } from '../../types'
import { formatDataBreve, formatKm, formatPrezzo } from '../../utils/format'
import { useAsync } from '../../utils/useAsync'
import { useFiltriUrl } from '../../utils/useFiltriUrl'

// Campi ammessi dal backend: CarService.ORDINAMENTI_ADMIN
const ORDINAMENTI: OpzioneOrdinamento[] = [
  { value: 'updatedAt,desc', label: 'Ultime modifiche' },
  { value: 'createdAt,desc', label: 'Ultimi creati' },
  { value: 'titolo,asc', label: 'Titolo A-Z' },
  { value: 'prezzo,asc', label: 'Prezzo crescente' },
  { value: 'prezzo,desc', label: 'Prezzo decrescente' },
  { value: 'statoAnnuncio,asc', label: 'Stato' },
]

function AzioneIcona({ icon, label, onClick, disabled }: { icon: IconName; label: string; onClick: () => void; disabled?: boolean }) {
  return (
    <OverlayTrigger placement="top" overlay={<Tooltip>{label}</Tooltip>}>
      <Button variant="outline-dark" className="btn-icon" onClick={onClick} disabled={disabled} aria-label={label}>
        <Icon name={icon} size={18} />
      </Button>
    </OverlayTrigger>
  )
}

/** Aggiorna una riga della tabella con la risposta completa del backend. */
const aggiornaRiga = (car: CarResponse) => (page: PageResponse<CarSummaryResponse>) => ({
  ...page,
  content: page.content.map((c) =>
    c.id === car.id
      ? { ...c, prezzo: car.prezzo, statoAnnuncio: car.statoAnnuncio, publishedAt: car.publishedAt, updatedAt: car.updatedAt }
      : c,
  ),
})

export default function AdminAnnunci() {
  const { valori, page, query, key, applica, vaiAPagina } = useFiltriUrl(ORDINAMENTI[0].value)
  const { data, error, loading, reload, setData } = useAsync(() => adminApi.elenco({ ...query, size: 20 }), key)
  const [prezzoDi, setPrezzoDi] = useState<CarSummaryResponse | null>(null)
  const [inCorso, setInCorso] = useState<string | null>(null)
  const [erroreAzione, setErroreAzione] = useState<string | null>(null)

  const cambiaStato = async (car: CarSummaryResponse) => {
    setInCorso(car.id)
    setErroreAzione(null)
    try {
      const aggiornata = car.statoAnnuncio === 'PUBBLICATO' ? await adminApi.bozza(car.id) : await adminApi.pubblica(car.id)
      setData(aggiornaRiga(aggiornata))
    } catch (err) {
      setErroreAzione(messaggioErrore(err))
    } finally {
      setInCorso(null)
    }
  }

  const azioni = (car: CarSummaryResponse) => (
    <div className="d-flex gap-2 justify-content-end">
      <Link to={`/admin/auto/${car.id}`} className="btn btn-outline-dark btn-icon" aria-label="Modifica">
        <Icon name="edit" size={18} />
      </Link>
      <AzioneIcona icon="euro" label="Cambia prezzo" onClick={() => setPrezzoDi(car)} />
      <AzioneIcona
        icon={car.statoAnnuncio === 'PUBBLICATO' ? 'eyeOff' : 'eye'}
        label={car.statoAnnuncio === 'PUBBLICATO' ? 'Ritira (bozza)' : 'Pubblica'}
        onClick={() => cambiaStato(car)}
        disabled={inCorso === car.id}
      />
    </div>
  )

  return (
    <Container>
      <PageHeader eyebrow="Area amministrazione" title="Annunci">
        <Link to="/admin/auto/nuova" className="btn btn-primary d-inline-flex align-items-center gap-2">
          <Icon name="plus" size={18} /> Nuovo annuncio
        </Link>
      </PageHeader>

      <FiltriAuto valori={valori} ordinamenti={ORDINAMENTI} conStato onApply={applica} />

      <div className="mt-4">
        <ErrorAlert message={error} onRetry={reload} />
        <ErrorAlert message={erroreAzione} />
      </div>
      {loading && !data && <Loader />}

      {data && data.content.length === 0 && (
        <EmptyState
          title="Nessun annuncio"
          text="Non ci sono annunci che corrispondono ai filtri."
          action={
            <Link to="/admin/auto/nuova" className="btn btn-primary">
              Crea il primo annuncio
            </Link>
          }
        />
      )}

      {data && data.content.length > 0 && (
        <div className={loading ? 'is-refreshing' : undefined}>
          <p className="eyebrow mb-3">
            {data.totalElements} {data.totalElements === 1 ? 'annuncio' : 'annunci'}
          </p>
          <div className="table-surface d-none d-md-block">
            <Table responsive hover className="admin-table mb-0 align-middle">
              <thead>
                <tr>
                  <th>Annuncio</th>
                  <th>Prezzo</th>
                  <th className="d-none d-xl-table-cell">Km</th>
                  <th>Stato</th>
                  <th className="d-none d-lg-table-cell">Aggiornato</th>
                  <th className="text-end">Azioni</th>
                </tr>
              </thead>
              <tbody>
                {data.content.map((car) => (
                  <tr key={car.id}>
                    <td>
                      <div className="d-flex align-items-center gap-3">
                        <div className="admin-thumb">
                          <CarImage src={car.copertina} alt="" />
                        </div>
                        <div>
                          <Link to={`/admin/auto/${car.id}`} className="fw-medium d-block">
                            {car.titolo}
                          </Link>
                          <span className="small text-secondary">
                            {car.marca} {car.modello} · {car.anno}
                          </span>
                        </div>
                      </div>
                    </td>
                    <td className="text-nowrap">{formatPrezzo(car.prezzo)}</td>
                    <td className="d-none d-xl-table-cell text-nowrap">{formatKm(car.chilometraggio)}</td>
                    <td>
                      <StatoBadge stato={car.statoAnnuncio} />
                    </td>
                    <td className="d-none d-lg-table-cell text-secondary small">{formatDataBreve(car.updatedAt)}</td>
                    <td>{azioni(car)}</td>
                  </tr>
                ))}
              </tbody>
            </Table>
          </div>

          {/* Da mobile la tabella diventa una lista di card, senza scroll orizzontale */}
          <div className="admin-cards d-md-none">
            {data.content.map((car) => (
              <article key={car.id} className="admin-card">
                <Link to={`/admin/auto/${car.id}`} className="admin-card-main">
                  <div className="admin-thumb">
                    <CarImage src={car.copertina} alt="" />
                  </div>
                  <div className="min-w-0">
                    <span className="fw-medium d-block">{car.titolo}</span>
                    <span className="small text-secondary">
                      {car.anno} · {formatKm(car.chilometraggio)}
                    </span>
                  </div>
                </Link>
                <div className="admin-card-footer">
                  <div className="d-flex align-items-center gap-2">
                    <span className="fw-medium">{formatPrezzo(car.prezzo)}</span>
                    <StatoBadge stato={car.statoAnnuncio} />
                  </div>
                  {azioni(car)}
                </div>
              </article>
            ))}
          </div>
          <Paginazione page={page} totalPages={data.totalPages} onChange={vaiAPagina} />
        </div>
      )}

      {prezzoDi && (
        <PrezzoModal
          show
          title="Aggiorna prezzo"
          label="Nuovo prezzo"
          help={
            prezzoDi.statoAnnuncio === 'PUBBLICATO'
              ? 'Gli utenti con una soglia superiore al nuovo prezzo riceveranno un avviso via email.'
              : "L'annuncio è in bozza: gli avvisi partiranno alla pubblicazione."
          }
          fieldName="prezzo"
          initial={prezzoDi.prezzo}
          onHide={() => setPrezzoDi(null)}
          onSave={(v) => adminApi.aggiornaPrezzo(prezzoDi.id, v).then((car) => setData(aggiornaRiga(car)))}
        >
          <p className="text-secondary mb-4">{prezzoDi.titolo}</p>
        </PrezzoModal>
      )}
    </Container>
  )
}
