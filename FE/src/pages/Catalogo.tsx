import { useMemo } from 'react'
import { Col, Container, Row } from 'react-bootstrap'
import { carsApi } from '../api'
import CarCard from '../components/CarCard'
import { EmptyState, ErrorAlert, Loader } from '../components/Feedback'
import FiltriAuto, { type OpzioneOrdinamento } from '../components/FiltriAuto'
import HeroShowcase from '../components/HeroShowcase'
import Paginazione from '../components/Paginazione'
import { InEvidenza, NastroMarche } from '../components/Vetrina'
import { useAsync } from '../utils/useAsync'
import { useFiltriUrl } from '../utils/useFiltriUrl'

// Campi ammessi dal backend: prezzo, chilometraggio, anno, publishedAt (CarService.ORDINAMENTI_PUBBLICI)
const ORDINAMENTI: OpzioneOrdinamento[] = [
  { value: 'publishedAt,desc', label: 'Più recenti' },
  { value: 'prezzo,asc', label: 'Prezzo crescente' },
  { value: 'prezzo,desc', label: 'Prezzo decrescente' },
  { value: 'chilometraggio,asc', label: 'Meno chilometri' },
  { value: 'anno,desc', label: 'Anno più recente' },
]

const AZZERA = { q: '', carburante: '', condizione: '', prezzoMin: '', prezzoMax: '' }

export default function Catalogo() {
  const { valori, page, query, key, applica, vaiAPagina } = useFiltriUrl(ORDINAMENTI[0].value)
  const { data, error, loading, reload } = useAsync(() => carsApi.cerca({ ...query, size: 12 }), key)

  // Vetrina (in evidenza + marche): le 50 vetture più prestigiose, caricate una volta sola
  const { data: vetrina } = useAsync(() => carsApi.cerca({ sort: 'prezzo,desc', size: 50 }), 'vetrina')
  const inEvidenza = vetrina?.content.find((c) => c.copertina) ?? null
  const marche = useMemo(() => {
    const conteggio = new Map<string, number>()
    vetrina?.content.forEach((c) => conteggio.set(c.marca, (conteggio.get(c.marca) ?? 0) + 1))
    return [...conteggio.entries()].sort((a, b) => b[1] - a[1]).map(([m]) => m)
  }, [vetrina])

  const filtriAttivi = [valori.q, valori.carburante, valori.condizione, valori.prezzoMin, valori.prezzoMax].some(Boolean)
  const mostraVetrina = !filtriAttivi && page === 0

  return (
    <>
      <section className="hero">
        <HeroShowcase />
        <Container className="hero-content">
          <p className="eyebrow text-white-50 mb-3">Salone Auto · Collezione</p>
          <h1 className="hero-title">
            Vetture <em>selezionate</em>,
            <br />
            una a una.
          </h1>
          <p className="hero-lead">
            Nuove, km 0 e usate garantite. Salva quelle che ami e ti avviseremo quando il prezzo scende.
          </p>
        </Container>
      </section>

      <Container className="catalogo">
        <FiltriAuto valori={valori} ordinamenti={ORDINAMENTI} onApply={applica} />

        {mostraVetrina && inEvidenza && <InEvidenza car={inEvidenza} />}
        {mostraVetrina && <NastroMarche marche={marche} onSelect={(m) => applica({ ...AZZERA, q: m })} />}

        <div className="results-header">
          <h2 className="h3 mb-0">
            {data ? (
              <>
                {data.totalElements} {data.totalElements === 1 ? 'vettura' : 'vetture'}
              </>
            ) : (
              'Catalogo'
            )}
          </h2>
          {loading && data && <span className="eyebrow">Aggiornamento…</span>}
        </div>

        <ErrorAlert message={error} onRetry={reload} />
        {loading && !data && <Loader />}

        {data && data.content.length === 0 && (
          <EmptyState
            title="Nessuna vettura trovata"
            text="Prova ad allargare la ricerca o ad azzerare i filtri."
            action={
              <button type="button" className="btn btn-outline-dark" onClick={() => applica(AZZERA)}>
                Azzera filtri
              </button>
            }
          />
        )}

        {data && data.content.length > 0 && (
          <div className={loading ? 'is-refreshing' : undefined}>
            <Row xs={1} md={2} xl={3} className="g-4">
              {data.content.map((car) => (
                <Col key={car.id}>
                  <CarCard car={car} />
                </Col>
              ))}
            </Row>
            <Paginazione page={page} totalPages={data.totalPages} onChange={vaiAPagina} />
          </div>
        )}
      </Container>
    </>
  )
}
