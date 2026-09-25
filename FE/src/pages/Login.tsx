import { useState, type FormEvent } from 'react'
import { Alert, Button, Form } from 'react-bootstrap'
import { Link, Navigate, useLocation } from 'react-router-dom'
import { authApi } from '../api'
import { ApiError, messaggioErrore } from '../api/client'
import AuthShell from '../components/AuthShell'
import FormField from '../components/FormField'
import { useAuth } from '../context/useAuth'
import { useApiForm } from '../utils/useApiForm'

export default function Login() {
  const { user, login } = useAuth()
  const location = useLocation()
  const from = (location.state as { from?: string } | null)?.from ?? '/'

  const [emailOrUsername, setEmailOrUsername] = useState('')
  const [password, setPassword] = useState('')
  const [nonVerificata, setNonVerificata] = useState(false)
  const [reinvio, setReinvio] = useState<{ ok: boolean; text: string } | null>(null)
  const { submitting, error, fieldErrors, run } = useApiForm()

  const onSubmit = async (e: FormEvent) => {
    e.preventDefault()
    setNonVerificata(false)
    setReinvio(null)
    try {
      await run(() => login({ emailOrUsername: emailOrUsername.trim(), password }))
    } catch (err) {
      // 403 dal backend = credenziali giuste ma email non ancora confermata
      if (err instanceof ApiError && err.status === 403) setNonVerificata(true)
    }
  }

  // Il reinvio richiede l'email: se l'utente ha usato lo username gli chiediamo di inserirla
  const reinviaVerifica = async () => {
    if (!emailOrUsername.includes('@')) {
      setReinvio({ ok: false, text: "Inserisci l'indirizzo email (non lo username) per ricevere un nuovo link." })
      return
    }
    try {
      const res = await authApi.resendVerification(emailOrUsername.trim())
      setReinvio({ ok: true, text: res.message })
    } catch (err) {
      setReinvio({ ok: false, text: messaggioErrore(err) })
    }
  }

  // Loggato (subito dopo il login o arrivando qui con una sessione attiva): torna alla pagina di provenienza.
  // Senza provenienza l'admin atterra sulla gestione annunci.
  if (user) {
    const home = user.ruoli.includes('ADMIN') ? '/admin' : '/'
    return <Navigate to={from === '/' ? home : from} replace />
  }

  return (
    <AuthShell eyebrow="Bentornato" title="Accedi" subtitle="Entra per salvare i preferiti e ricevere gli avvisi di prezzo.">
      {error && (
        <Alert variant={nonVerificata ? 'dark' : 'danger'}>
          <div className={nonVerificata ? 'fw-medium mb-1' : ''}>{nonVerificata ? 'Email non verificata' : error}</div>
          {nonVerificata && (
            <>
              <div className="small opacity-75 mb-3">{error}</div>
              <Button size="sm" className="btn-ghost-light" onClick={reinviaVerifica}>
                Invia di nuovo l'email
              </Button>
            </>
          )}
        </Alert>
      )}
      {reinvio && <Alert variant={reinvio.ok ? 'light' : 'danger'}>{reinvio.text}</Alert>}

      <Form noValidate onSubmit={onSubmit}>
        <FormField
          label="Email o username"
          name="emailOrUsername"
          autoComplete="username"
          value={emailOrUsername}
          onChange={(e) => setEmailOrUsername(e.target.value)}
          error={fieldErrors.emailOrUsername}
          required
        />
        <FormField
          label="Password"
          name="password"
          type="password"
          autoComplete="current-password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          error={fieldErrors.password}
          className="mb-2"
          required
        />
        <div className="text-end mb-4">
          <Link to="/password-dimenticata" className="small link-underline">
            Password dimenticata?
          </Link>
        </div>
        <Button type="submit" className="w-100" disabled={submitting}>
          {submitting ? 'Accesso in corso…' : 'Accedi'}
        </Button>
      </Form>

      <p className="text-center text-secondary small mt-4 mb-0">
        Non hai un account?{' '}
        <Link to="/registrati" className="link-underline">
          Registrati
        </Link>
      </p>
    </AuthShell>
  )
}
