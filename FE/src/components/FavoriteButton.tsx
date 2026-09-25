import { useState, type MouseEvent } from 'react'
import { Button } from 'react-bootstrap'
import { useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/useAuth'
import { useFavorites } from '../context/useFavorites'
import Icon from './Icon'

/**
 * Cuore dei preferiti. Per gli ospiti porta al login (e poi di nuovo qui).
 * variant "icon": pulsante tondo sopra la copertina; "full": pulsante con testo nel dettaglio.
 */
export default function FavoriteButton({ carId, variant = 'icon' }: { carId: string; variant?: 'icon' | 'full' }) {
  const { user } = useAuth()
  const { isFavorite, toggle } = useFavorites()
  const navigate = useNavigate()
  const location = useLocation()
  const [busy, setBusy] = useState(false)
  const [errore, setErrore] = useState(false)
  const attivo = isFavorite(carId)

  const onClick = async (e: MouseEvent) => {
    // Il pulsante sta dentro la card cliccabile: non aprire il dettaglio
    e.preventDefault()
    e.stopPropagation()
    if (!user) {
      navigate('/login', { state: { from: location.pathname + location.search } })
      return
    }
    setBusy(true)
    setErrore(false)
    try {
      await toggle(carId)
    } catch {
      setErrore(true)
    } finally {
      setBusy(false)
    }
  }

  const label = attivo ? 'Rimuovi dai preferiti' : 'Aggiungi ai preferiti'

  if (variant === 'full') {
    return (
      <>
        <Button
          variant={attivo ? 'outline-dark' : 'primary'}
          className="w-100 d-inline-flex align-items-center justify-content-center gap-2"
          onClick={onClick}
          disabled={busy}
          aria-pressed={attivo}
        >
          <Icon name="heart" size={18} filled={attivo} />
          {attivo ? 'Nei tuoi preferiti' : 'Aggiungi ai preferiti'}
        </Button>
        {errore && <p className="small text-secondary mt-2 mb-0">Operazione non riuscita, riprova.</p>}
      </>
    )
  }

  return (
    <button
      type="button"
      className={`fav-btn${attivo ? ' is-active' : ''}`}
      onClick={onClick}
      disabled={busy}
      aria-pressed={attivo}
      aria-label={label}
      title={errore ? 'Operazione non riuscita, riprova' : label}
    >
      <Icon name="heart" size={18} filled={attivo} />
    </button>
  )
}
