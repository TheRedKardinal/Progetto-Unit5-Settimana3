import { useState, type FormEvent } from 'react'
import { Alert, Button, Form } from 'react-bootstrap'
import { Link, useSearchParams } from 'react-router-dom'
import { authApi } from '../api'
import AuthShell from '../components/AuthShell'
import FormField from '../components/FormField'
import { useApiForm } from '../utils/useApiForm'

/** Atterraggio del link di reset: /reset-password?token=... */
export default function ResetPassword() {
  const [params] = useSearchParams()
  const token = params.get('token') ?? ''
  const [password, setPassword] = useState('')
  const [conferma, setConferma] = useState('')
  const [erroreConferma, setErroreConferma] = useState<string | undefined>()
  const [messaggio, setMessaggio] = useState<string | null>(null)
  const { submitting, error, fieldErrors, submit } = useApiForm()

  const onSubmit = async (e: FormEvent) => {
    e.preventDefault()
    if (password !== conferma) {
      setErroreConferma('Le due password non coincidono')
      return
    }
    setErroreConferma(undefined)
    const res = await submit(() => authApi.resetPassword(token, password))
    if (res) setMessaggio(res.message)
  }

  if (!token) {
    return (
      <AuthShell eyebrow="Recupero accesso" title="Link non valido">
        <p className="text-secondary">Il link non contiene il token di reset. Richiedine uno nuovo.</p>
        <Link to="/password-dimenticata" className="btn btn-primary">
          Richiedi un nuovo link
        </Link>
      </AuthShell>
    )
  }

  return (
    <AuthShell eyebrow="Recupero accesso" title="Nuova password">
      {messaggio ? (
        <>
          <Alert variant="light">{messaggio}</Alert>
          <Link to="/login" className="btn btn-primary">
            Accedi
          </Link>
        </>
      ) : (
        <>
          {error && <Alert variant="danger">{error}</Alert>}
          <Form noValidate onSubmit={onSubmit}>
            <FormField
              label="Nuova password"
              name="nuovaPassword"
              type="password"
              autoComplete="new-password"
              maxLength={64}
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              error={fieldErrors.nuovaPassword}
              hint="Da 8 a 64 caratteri."
              required
            />
            <FormField
              label="Conferma password"
              name="conferma"
              type="password"
              autoComplete="new-password"
              maxLength={64}
              value={conferma}
              onChange={(e) => setConferma(e.target.value)}
              error={erroreConferma}
              className="mb-4"
              required
            />
            <Button type="submit" className="w-100" disabled={submitting}>
              {submitting ? 'Salvataggio…' : 'Imposta password'}
            </Button>
          </Form>
          {fieldErrors.token && <p className="small text-secondary mt-3 mb-0">{fieldErrors.token}</p>}
        </>
      )}
    </AuthShell>
  )
}
