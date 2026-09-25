import { useEffect, useRef, useState } from 'react'
import { Alert } from 'react-bootstrap'
import { Link, useSearchParams } from 'react-router-dom'
import { authApi } from '../api'
import { messaggioErrore } from '../api/client'
import AuthShell from '../components/AuthShell'
import { Loader } from '../components/Feedback'

/** Atterraggio del link inviato via email alla registrazione: /verifica-email?token=... */
export default function VerificaEmail() {
  const [params] = useSearchParams()
  const token = params.get('token')
  const [esito, setEsito] = useState<{ ok: boolean; text: string } | null>(
    token ? null : { ok: false, text: 'Link non valido: token mancante.' },
  )
  // Il token è monouso: in StrictMode l'effetto gira due volte e la seconda chiamata fallirebbe
  const inviato = useRef(false)

  useEffect(() => {
    if (!token || inviato.current) return
    inviato.current = true
    authApi
      .verifyEmail(token)
      .then((res) => setEsito({ ok: true, text: res.message }))
      .catch((err) => setEsito({ ok: false, text: messaggioErrore(err) }))
  }, [token])

  return (
    <AuthShell eyebrow="Verifica email" title={esito?.ok ? 'Email confermata' : 'Conferma account'}>
      {!esito && <Loader label="Verifica in corso" />}
      {esito && <Alert variant={esito.ok ? 'light' : 'danger'}>{esito.text}</Alert>}
      {esito?.ok && (
        <Link to="/login" className="btn btn-primary">
          Accedi
        </Link>
      )}
      {esito && !esito.ok && (
        <p className="text-secondary small mb-0">
          Il link potrebbe essere scaduto o già usato. Prova ad{' '}
          <Link to="/login" className="link-underline">
            accedere
          </Link>
          : se l'email non è verificata potrai richiedere un nuovo link.
        </p>
      )}
    </AuthShell>
  )
}
