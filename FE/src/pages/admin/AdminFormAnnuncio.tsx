import { useState, type ChangeEvent, type FormEvent } from 'react'
import { Alert, Button, Col, Container, Form, Row } from 'react-bootstrap'
import { Link, useLocation, useNavigate, useParams } from 'react-router-dom'
import { adminApi } from '../../api'
import { messaggioErrore } from '../../api/client'
import { BackLink, ErrorAlert, Loader } from '../../components/Feedback'
import FormField from '../../components/FormField'
import ImmaginiEditor from '../../components/ImmaginiEditor'
import StatoBadge from '../../components/StatoBadge'
import { CARBURANTI, CONDIZIONI, type CarRequest, type CarResponse, type VinLookupResponse } from '../../types'
import { CARBURANTE_LABEL, CONDIZIONE_LABEL, formatData, toNumberOrNull } from '../../utils/format'

// I messaggi di esito stanno in cima alla pagina, mentre i pulsanti sono nella barra fissa in basso
const vaiInCima = () => window.scrollTo({ top: 0, behavior: 'smooth' })
import { useApiForm } from '../../utils/useApiForm'
import { useAsync } from '../../utils/useAsync'

// Stesso pattern di CarRequest.vin: 17 caratteri, senza I, O, Q
const VIN_VALIDO = /^[A-HJ-NPR-Z0-9]{17}$/i

interface FormState {
  vin: string
  marca: string
  modello: string
  anno: string
  titolo: string
  descrizione: string
  chilometraggio: string
  carburante: string
  prezzo: string
  condizione: string
  immagini: string[]
}

const VUOTO: FormState = {
  vin: '',
  marca: '',
  modello: '',
  anno: '',
  titolo: '',
  descrizione: '',
  chilometraggio: '',
  carburante: '',
  prezzo: '',
  condizione: '',
  immagini: [],
}

const daAuto = (car: CarResponse): FormState => ({
  vin: car.vin ?? '',
  marca: car.marca,
  modello: car.modello,
  anno: String(car.anno),
  titolo: car.titolo,
  descrizione: car.descrizione,
  chilometraggio: String(car.chilometraggio),
  carburante: car.carburante,
  prezzo: String(car.prezzo),
  condizione: car.condizione,
  immagini: car.immagini,
})

// I campi vuoti diventano null: è il backend a rispondere con i messaggi di validazione per campo
const aRichiesta = (f: FormState): CarRequest => ({
  vin: f.vin.trim(),
  marca: f.marca.trim(),
  modello: f.modello.trim(),
  anno: toNumberOrNull(f.anno),
  titolo: f.titolo.trim(),
  descrizione: f.descrizione.trim(),
  chilometraggio: toNumberOrNull(f.chilometraggio),
  carburante: (f.carburante || null) as CarRequest['carburante'],
  prezzo: toNumberOrNull(f.prezzo),
  condizione: (f.condizione || null) as CarRequest['condizione'],
  immagini: f.immagini,
})

/** /admin/auto/nuova e /admin/auto/:id */
export default function AdminFormAnnuncio() {
  const { id } = useParams()
  if (!id) return <FormAnnuncio />
  return <CaricaAnnuncio id={id} />
}

function CaricaAnnuncio({ id }: { id: string }) {
  const { data, error, reload } = useAsync(() => adminApi.dettaglio(id), id)
  if (error && !data) {
    return (
      <Container className="section">
        <BackLink to="/admin">Annunci</BackLink>
        <div className="mt-4">
          <ErrorAlert message={error} onRetry={reload} />
        </div>
      </Container>
    )
  }
  if (!data) return <Loader />
  return <FormAnnuncio iniziale={data} />
}

function FormAnnuncio({ iniziale }: { iniziale?: CarResponse }) {
  const navigate = useNavigate()
  const location = useLocation()
  const [car, setCar] = useState<CarResponse | undefined>(iniziale)
  const [form, setForm] = useState<FormState>(iniziale ? daAuto(iniziale) : VUOTO)
  const [successo, setSuccesso] = useState<string | null>(
    (location.state as { creato?: boolean } | null)?.creato ? 'Annuncio creato come bozza.' : null,
  )
  const { submitting, error, fieldErrors, submit } = useApiForm()

  const [vinInCorso, setVinInCorso] = useState(false)
  const [vinEsito, setVinEsito] = useState<{ ok: boolean; text: string } | null>(null)
  const [statoInCorso, setStatoInCorso] = useState(false)
  const [erroreStato, setErroreStato] = useState<string | null>(null)

  const set = (patch: Partial<FormState>) => setForm((f) => ({ ...f, ...patch }))
  const onChange = (e: ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) =>
    set({ [e.target.name]: e.target.value })

  const onSubmit = async (e: FormEvent) => {
    e.preventDefault()
    setSuccesso(null)
    const req = aRichiesta(form)
    const salvata = await submit(() => (car ? adminApi.aggiorna(car.id, req) : adminApi.crea(req)))
    if (!salvata) {
      vaiInCima()
      return
    }
    if (!car) {
      navigate(`/admin/auto/${salvata.id}`, { replace: true, state: { creato: true } })
      return
    }
    setCar(salvata)
    setForm(daAuto(salvata))
    setSuccesso('Modifiche salvate.')
    vaiInCima()
  }

  /** Precompila marca, modello, anno, carburante e foto; titolo, descrizione e prezzo restano dell'admin. */
  const cercaVin = async () => {
    const vin = form.vin.trim().toUpperCase()
    if (!VIN_VALIDO.test(vin)) {
      setVinEsito({ ok: false, text: 'Il VIN deve avere 17 caratteri alfanumerici (senza I, O, Q).' })
      return
    }
    setVinInCorso(true)
    setVinEsito(null)
    try {
      const r: VinLookupResponse = await adminApi.cercaVin(vin)
      setForm((f) => ({
        ...f,
        vin: r.vin,
        marca: r.marca ?? f.marca,
        modello: r.modello ?? f.modello,
        anno: r.anno != null ? String(r.anno) : f.anno,
        carburante: r.carburante ?? f.carburante,
        titolo: f.titolo || [r.marca, r.modello].filter(Boolean).join(' '),
        immagini: [...f.immagini, ...r.immagini.filter((u) => !f.immagini.includes(u))].slice(0, 20),
      }))
      const dettagli = [r.motore && `motore ${r.motore}`, !r.carburante && 'carburante da scegliere a mano']
        .filter(Boolean)
        .join(' · ')
      setVinEsito({ ok: true, text: `Dati trovati per ${r.vin}${dettagli ? ` (${dettagli})` : ''}. Controllali prima di salvare.` })
    } catch (err) {
      setVinEsito({ ok: false, text: messaggioErrore(err) })
    } finally {
      setVinInCorso(false)
    }
  }

  const cambiaStato = async () => {
    if (!car) return
    setStatoInCorso(true)
    setErroreStato(null)
    setSuccesso(null)
    try {
      const aggiornata = car.statoAnnuncio === 'PUBBLICATO' ? await adminApi.bozza(car.id) : await adminApi.pubblica(car.id)
      setCar(aggiornata)
      setSuccesso(aggiornata.statoAnnuncio === 'PUBBLICATO' ? 'Annuncio pubblicato nel catalogo.' : 'Annuncio ritirato: ora è in bozza.')
    } catch (err) {
      setErroreStato(messaggioErrore(err))
    } finally {
      setStatoInCorso(false)
      vaiInCima()
    }
  }

  const pubblicato = car?.statoAnnuncio === 'PUBBLICATO'

  return (
    <Container className="section admin-form">
      <BackLink to="/admin">Annunci</BackLink>
      <div className="d-flex flex-wrap align-items-end justify-content-between gap-3 mt-3 mb-4">
        <div>
          <h1 className="display-5 mb-1">{car ? car.titolo : 'Nuovo annuncio'}</h1>
          {car && (
            <p className="text-secondary small mb-0">
              Creato il {formatData(car.createdAt)} · ultima modifica {formatData(car.updatedAt)}
              {car.publishedAt && <> · pubblicato il {formatData(car.publishedAt)}</>}
            </p>
          )}
        </div>
        {car && (
          <div className="d-flex align-items-center gap-2">
            <StatoBadge stato={car.statoAnnuncio} />
            {pubblicato && (
              <Link to={`/auto/${car.id}`} className="btn btn-sm btn-outline-dark">
                Vedi nel catalogo
              </Link>
            )}
          </div>
        )}
      </div>

      {successo && <Alert variant="light">{successo}</Alert>}
      {error && <Alert variant="danger">{error}</Alert>}
      <ErrorAlert message={erroreStato} />

      <Form noValidate onSubmit={onSubmit}>
        <section className="form-section surface bg-carbone">
          <Row className="g-3 align-items-end">
            <Col lg={5}>
              <p className="eyebrow text-white-50 mb-2">Precompila da VIN</p>
              <p className="text-white-50 small mb-0">
                Marca, modello, anno, carburante e foto vengono compilati automaticamente. Il VIN è facoltativo.
              </p>
            </Col>
            <Col lg={7}>
              <Form.Label htmlFor="f-vin" className="text-white-50">
                VIN
              </Form.Label>
              <div className="d-flex gap-2">
                <Form.Control
                  id="f-vin"
                  name="vin"
                  className="font-monospace text-uppercase vin-input"
                  maxLength={17}
                  placeholder="17 caratteri"
                  value={form.vin}
                  onChange={onChange}
                  isInvalid={!!fieldErrors.vin}
                />
                <Button className="btn-ghost-light text-nowrap" onClick={cercaVin} disabled={vinInCorso || !form.vin.trim()}>
                  {vinInCorso ? 'Ricerca…' : 'Cerca VIN'}
                </Button>
              </div>
              {fieldErrors.vin && <div className="small mt-2 text-white">— {fieldErrors.vin}</div>}
              {vinEsito && <div className={`small mt-2 ${vinEsito.ok ? 'text-white-50' : 'text-white'}`}>{vinEsito.text}</div>}
            </Col>
          </Row>
        </section>

        <section className="form-section surface">
          <h2 className="h3 mb-4">Veicolo</h2>
          <Row className="g-3">
            <Col md={6}>
              <FormField label="Marca" name="marca" maxLength={60} value={form.marca} onChange={onChange} error={fieldErrors.marca} className="" />
            </Col>
            <Col md={6}>
              <FormField label="Modello" name="modello" maxLength={80} value={form.modello} onChange={onChange} error={fieldErrors.modello} className="" />
            </Col>
            <Col md={4}>
              <FormField label="Anno" name="anno" type="number" min={1900} max={2100} value={form.anno} onChange={onChange} error={fieldErrors.anno} className="" />
            </Col>
            <Col md={4}>
              <Form.Group controlId="f-carburante">
                <Form.Label>Carburante</Form.Label>
                <Form.Select name="carburante" value={form.carburante} onChange={onChange} isInvalid={!!fieldErrors.carburante}>
                  <option value="">Seleziona…</option>
                  {CARBURANTI.map((c) => (
                    <option key={c} value={c}>
                      {CARBURANTE_LABEL[c]}
                    </option>
                  ))}
                </Form.Select>
                <Form.Control.Feedback type="invalid">{fieldErrors.carburante}</Form.Control.Feedback>
              </Form.Group>
            </Col>
            <Col md={4}>
              <Form.Group controlId="f-condizione">
                <Form.Label>Condizione</Form.Label>
                <Form.Select name="condizione" value={form.condizione} onChange={onChange} isInvalid={!!fieldErrors.condizione}>
                  <option value="">Seleziona…</option>
                  {CONDIZIONI.map((c) => (
                    <option key={c} value={c}>
                      {CONDIZIONE_LABEL[c]}
                    </option>
                  ))}
                </Form.Select>
                <Form.Control.Feedback type="invalid">{fieldErrors.condizione}</Form.Control.Feedback>
              </Form.Group>
            </Col>
            <Col md={6}>
              <FormField
                label="Chilometraggio (km)"
                name="chilometraggio"
                type="number"
                min={0}
                step="0.1"
                value={form.chilometraggio}
                onChange={onChange}
                error={fieldErrors.chilometraggio}
                className=""
              />
            </Col>
          </Row>
        </section>

        <section className="form-section surface">
          <h2 className="h3 mb-4">Annuncio</h2>
          <FormField label="Titolo" name="titolo" maxLength={150} value={form.titolo} onChange={onChange} error={fieldErrors.titolo} />
          <FormField
            label="Descrizione"
            name="descrizione"
            as="textarea"
            rows={7}
            maxLength={10000}
            value={form.descrizione}
            onChange={onChange}
            error={fieldErrors.descrizione}
            hint={`${form.descrizione.length}/10000 caratteri`}
          />
          <Row>
            <Col md={6}>
              <FormField
                label="Prezzo (€)"
                name="prezzo"
                type="number"
                min={0}
                step="0.01"
                value={form.prezzo}
                onChange={onChange}
                error={fieldErrors.prezzo}
                hint={pubblicato ? 'Se il prezzo scende, gli utenti con una soglia superiore riceveranno un avviso.' : undefined}
                className=""
              />
            </Col>
          </Row>
        </section>

        <section className="form-section surface">
          <h2 className="h3 mb-4">Immagini</h2>
          <ImmaginiEditor
            value={form.immagini}
            onChange={(immagini) => set({ immagini })}
            error={fieldErrors.immagini ?? Object.entries(fieldErrors).find(([k]) => k.startsWith('immagini['))?.[1]}
          />
        </section>

        <div className="form-actions surface">
          <Link to="/admin" className="btn btn-outline-dark d-none d-sm-inline-block">
            Annulla
          </Link>
          <div className="form-actions-main">
            {car && (
              <Button variant="outline-dark" onClick={cambiaStato} disabled={statoInCorso || submitting}>
                {pubblicato ? 'Ritira (bozza)' : 'Pubblica'}
              </Button>
            )}
            <Button type="submit" disabled={submitting}>
              {submitting ? 'Salvataggio…' : car ? 'Salva modifiche' : 'Salva bozza'}
            </Button>
          </div>
        </div>
      </Form>
    </Container>
  )
}
