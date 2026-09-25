import { useState, type FormEvent } from 'react'
import { Alert, Button, Form } from 'react-bootstrap'
import { Link } from 'react-router-dom'
import { authApi } from '../api'
import AuthShell from '../components/AuthShell'
import FormField from '../components/FormField'
import { useApiForm } from '../utils/useApiForm'

export default function PasswordDimenticata() {
  const [email, setEmail] = useState('')
  const [messaggio, setMessaggio] = useState<string | null>(null)
  const { submitting, error, fieldErrors, submit } = useApiForm()

  const onSubmit = async (e: FormEvent) => {
    e.preventDefault()
    const res = await submit(() => authApi.forgotPassword(email.trim()))
    if (res) setMessaggio(res.message)
  }

  return (
    <AuthShell
      eyebrow="Recupero accesso"
      title="Password dimenticata"
      subtitle="Inserisci l'email dell'account: ti invieremo un link per sceglierne una nuova."
    >
      {messaggio ? (
        <Alert variant="light">{messaggio}</Alert>
      ) : (
        <>
          {error && <Alert variant="danger">{error}</Alert>}
          <Form noValidate onSubmit={onSubmit}>
            <FormField
              label="Email"
              name="email"
              type="email"
              autoComplete="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              error={fieldErrors.email}
              className="mb-4"
              required
            />
            <Button type="submit" className="w-100" disabled={submitting}>
              {submitting ? 'Invio in corso…' : 'Invia il link'}
            </Button>
          </Form>
        </>
      )}
      <p className="text-center small mt-4 mb-0">
        <Link to="/login" className="link-underline">
          Torna al login
        </Link>
      </p>
    </AuthShell>
  )
}
