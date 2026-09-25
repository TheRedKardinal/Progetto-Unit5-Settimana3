import { useState, type KeyboardEvent } from 'react'
import { Button, Form, InputGroup } from 'react-bootstrap'
import CarImage from './CarImage'
import Icon from './Icon'

const MAX_IMMAGINI = 20
const URL_VALIDO = /^https?:\/\/\S+$/

/** Lista ordinata di URL immagine (la prima è la copertina), con gli stessi vincoli di CarRequest. */
export default function ImmaginiEditor({
  value,
  onChange,
  error,
}: {
  value: string[]
  onChange: (immagini: string[]) => void
  error?: string
}) {
  const [url, setUrl] = useState('')
  const [erroreUrl, setErroreUrl] = useState<string | null>(null)

  const aggiungi = () => {
    const pulito = url.trim()
    if (!URL_VALIDO.test(pulito)) {
      setErroreUrl("URL non valido: deve iniziare con http:// o https://")
      return
    }
    if (value.includes(pulito)) {
      setErroreUrl('Immagine già presente')
      return
    }
    onChange([...value, pulito])
    setUrl('')
    setErroreUrl(null)
  }

  const onKeyDown = (e: KeyboardEvent<HTMLInputElement>) => {
    // Invio aggiunge l'immagine invece di inviare il form dell'annuncio
    if (e.key === 'Enter') {
      e.preventDefault()
      aggiungi()
    }
  }

  const sposta = (da: number, a: number) => {
    const next = [...value]
    const [item] = next.splice(da, 1)
    next.splice(a, 0, item)
    onChange(next)
  }

  const pieno = value.length >= MAX_IMMAGINI

  return (
    <div>
      <Form.Label htmlFor="nuova-immagine">Aggiungi immagine (URL)</Form.Label>
      <InputGroup hasValidation className="mb-2">
        <Form.Control
          id="nuova-immagine"
          type="url"
          placeholder="https://…"
          value={url}
          onChange={(e) => setUrl(e.target.value)}
          onKeyDown={onKeyDown}
          isInvalid={!!erroreUrl}
          disabled={pieno}
        />
        <Button variant="outline-dark" onClick={aggiungi} disabled={pieno || !url.trim()} className="input-group-btn">
          Aggiungi
        </Button>
        <Form.Control.Feedback type="invalid">{erroreUrl}</Form.Control.Feedback>
      </InputGroup>
      <Form.Text className="text-secondary d-block mb-3">
        {value.length}/{MAX_IMMAGINI} immagini · la prima è la copertina del catalogo.
      </Form.Text>
      {error && <div className="invalid-feedback d-block mb-3">{error}</div>}

      {value.length > 0 && (
        <div className="img-grid">
          {value.map((src, i) => (
            <figure key={src} className="img-tile">
              <CarImage src={src} alt={`Immagine ${i + 1}`} />
              {i === 0 && <span className="img-tile-badge">Copertina</span>}
              <div className="img-tile-actions">
                <button type="button" onClick={() => sposta(i, i - 1)} disabled={i === 0} aria-label="Sposta a sinistra">
                  <Icon name="chevronLeft" size={16} />
                </button>
                <button
                  type="button"
                  onClick={() => sposta(i, i + 1)}
                  disabled={i === value.length - 1}
                  aria-label="Sposta a destra"
                >
                  <Icon name="chevronRight" size={16} />
                </button>
                <button type="button" onClick={() => onChange(value.filter((_, j) => j !== i))} aria-label="Rimuovi">
                  <Icon name="close" size={16} />
                </button>
              </div>
            </figure>
          ))}
        </div>
      )}
    </div>
  )
}
