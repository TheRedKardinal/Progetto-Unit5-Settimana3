import { useState, type ChangeEvent, type FormEvent } from 'react'
import { Alert, Button, Col, Form, Row } from 'react-bootstrap'
import { Link } from 'react-router-dom'
import { authApi } from '../api'
import AuthShell from '../components/AuthShell'
import FormField from '../components/FormField'
import type { RegisterRequest } from '../types'
import { useApiForm } from '../utils/useApiForm'

const VUOTO: RegisterRequest = { nome: '', cognome: '', username: '', email: '', password: '' }

export default function Registrati() {
  const [form, setForm] = useState<RegisterRequest>(VUOTO)
  const [registrato, setRegistrato] = useState<string | null>(null)
  const { submitting, error, fieldErrors, submit } = useApiForm()

  const onChange = (e: ChangeEvent<HTMLInputElement>) => setForm({ ...form, [e.target.name]: e.target.value })

  const onSubmit = async (e: FormEvent) => {
    e.preventDefault()
    const user = await submit(() =>
      authApi.register({ ...form, nome: form.nome.trim(), cognome: form.cognome.trim(), email: form.email.trim() }),
    )
    if (user) setRegistrato(user.email)
  }

  if (registrato) {
    return (
      <AuthShell eyebrow="Quasi fatto" title="Controlla la tua email">
        <p className="text-secondary">
          Ti abbiamo inviato un link di conferma a <strong className="text-dark">{registrato}</strong>. Aprilo per
          attivare l'account, poi potrai accedere.
        </p>
        <Link to="/login" className="btn btn-primary mt-3">
          Vai al login
        </Link>
      </AuthShell>
    )
  }

  return (
    <AuthShell eyebrow="Nuovo account" title="Registrati" subtitle="Salva le vetture che ti interessano e fatti avvisare quando il prezzo scende.">
      {error && <Alert variant="danger">{error}</Alert>}
      <Form noValidate onSubmit={onSubmit}>
        <Row className="g-3 mb-3">
          <Col sm={6}>
            <FormField label="Nome" name="nome" autoComplete="given-name" maxLength={100} value={form.nome} onChange={onChange} error={fieldErrors.nome} className="" required />
          </Col>
          <Col sm={6}>
            <FormField label="Cognome" name="cognome" autoComplete="family-name" maxLength={100} value={form.cognome} onChange={onChange} error={fieldErrors.cognome} className="" required />
          </Col>
        </Row>
        <FormField
          label="Username"
          name="username"
          autoComplete="username"
          maxLength={50}
          value={form.username}
          onChange={onChange}
          error={fieldErrors.username}
          hint="Da 3 a 50 caratteri: lettere, numeri, punto, trattino e underscore."
          required
        />
        <FormField label="Email" name="email" type="email" autoComplete="email" maxLength={255} value={form.email} onChange={onChange} error={fieldErrors.email} required />
        <FormField
          label="Password"
          name="password"
          type="password"
          autoComplete="new-password"
          maxLength={64}
          value={form.password}
          onChange={onChange}
          error={fieldErrors.password}
          hint="Da 8 a 64 caratteri."
          className="mb-4"
          required
        />
        <Button type="submit" className="w-100" disabled={submitting}>
          {submitting ? 'Registrazione in corso…' : 'Crea account'}
        </Button>
      </Form>
      <p className="text-center text-secondary small mt-4 mb-0">
        Hai già un account?{' '}
        <Link to="/login" className="link-underline">
          Accedi
        </Link>
      </p>
    </AuthShell>
  )
}
