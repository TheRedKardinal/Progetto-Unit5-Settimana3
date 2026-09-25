import type { ComponentProps } from 'react'
import { Form } from 'react-bootstrap'

type Props = ComponentProps<typeof Form.Control> & {
  label: string
  name: string
  error?: string
  hint?: string
}

/** Campo di form con label e messaggio d'errore per campo (da ProblemDetail.errors del backend). */
export default function FormField({ label, name, error, hint, className, ...rest }: Props) {
  return (
    <Form.Group className={className ?? 'mb-3'} controlId={`f-${name}`}>
      <Form.Label>{label}</Form.Label>
      <Form.Control name={name} isInvalid={!!error} {...rest} />
      {hint && !error && <Form.Text className="text-secondary">{hint}</Form.Text>}
      <Form.Control.Feedback type="invalid">{error}</Form.Control.Feedback>
    </Form.Group>
  )
}
