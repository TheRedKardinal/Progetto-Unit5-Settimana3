import { useState, type FormEvent, type ReactNode } from 'react'
import { Alert, Button, Form, InputGroup, Modal } from 'react-bootstrap'
import { toNumberOrNull } from '../utils/format'
import { useApiForm } from '../utils/useApiForm'

/**
 * Modal con un solo importo in euro: usata per il prezzo di un annuncio (admin)
 * e per la soglia di avviso di un preferito. Con onRemove compare "Rimuovi" (soglia = null).
 */
export default function PrezzoModal({
  show,
  title,
  label,
  help,
  initial,
  fieldName,
  onHide,
  onSave,
  onRemove,
  children,
}: {
  show: boolean
  title: string
  label: string
  help?: string
  initial: number | null
  /** Nome del campo negli errori di validazione del backend (es. "prezzo", "sogliaPrezzo") */
  fieldName: string
  onHide: () => void
  onSave: (valore: number) => Promise<unknown>
  onRemove?: () => Promise<unknown>
  children?: ReactNode
}) {
  const [valore, setValore] = useState(initial != null ? String(initial) : '')
  const [erroreLocale, setErroreLocale] = useState<string | null>(null)
  const { submitting, error, fieldErrors, submit } = useApiForm()

  const onSubmit = async (e: FormEvent) => {
    e.preventDefault()
    const n = toNumberOrNull(valore)
    if (n == null || n <= 0) {
      setErroreLocale("Inserisci un importo maggiore di zero")
      return
    }
    setErroreLocale(null)
    const ok = await submit(async () => {
      await onSave(n)
      return true
    })
    if (ok) onHide()
  }

  const rimuovi = async () => {
    if (!onRemove) return
    const ok = await submit(async () => {
      await onRemove()
      return true
    })
    if (ok) onHide()
  }

  const erroreCampo = erroreLocale ?? fieldErrors[fieldName]

  return (
    <Modal show={show} onHide={onHide} centered>
      <Form noValidate onSubmit={onSubmit}>
        <Modal.Header closeButton>
          <Modal.Title>{title}</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          {children}
          {error && !erroreCampo && <Alert variant="danger">{error}</Alert>}
          <Form.Group controlId="prezzo-modal">
            <Form.Label>{label}</Form.Label>
            <InputGroup hasValidation>
              <InputGroup.Text>€</InputGroup.Text>
              <Form.Control
                type="number"
                min={0}
                step="0.01"
                inputMode="decimal"
                value={valore}
                onChange={(e) => setValore(e.target.value)}
                isInvalid={!!erroreCampo}
                autoFocus
              />
              <Form.Control.Feedback type="invalid">{erroreCampo}</Form.Control.Feedback>
            </InputGroup>
            {help && <Form.Text className="text-secondary">{help}</Form.Text>}
          </Form.Group>
        </Modal.Body>
        <Modal.Footer className="justify-content-between">
          {onRemove && initial != null ? (
            <Button variant="link" className="text-secondary text-decoration-none px-0" onClick={rimuovi} disabled={submitting}>
              Rimuovi soglia
            </Button>
          ) : (
            <Button variant="outline-dark" onClick={onHide} disabled={submitting}>
              Annulla
            </Button>
          )}
          <Button type="submit" disabled={submitting}>
            {submitting ? 'Salvataggio…' : 'Salva'}
          </Button>
        </Modal.Footer>
      </Form>
    </Modal>
  )
}
