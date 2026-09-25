import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '../context/useAuth'
import { Loader } from './Feedback'

/** Solo utenti loggati: altrimenti al login, con ritorno alla pagina richiesta. */
export function RequireAuth() {
  const { user, loading } = useAuth()
  const location = useLocation()
  if (loading) return <Loader />
  if (!user) return <Navigate to="/login" replace state={{ from: location.pathname + location.search }} />
  return <Outlet />
}

/** Solo ADMIN. */
export function RequireAdmin() {
  const { user, isAdmin, loading } = useAuth()
  const location = useLocation()
  if (loading) return <Loader />
  if (!user) return <Navigate to="/login" replace state={{ from: location.pathname + location.search }} />
  if (!isAdmin) return <Navigate to="/" replace />
  return <Outlet />
}

/** Pagine per ospiti (login, registrazione): chi è già loggato torna al catalogo. */
export function GuestOnly() {
  const { user, loading } = useAuth()
  if (loading) return <Loader />
  if (user) return <Navigate to="/" replace />
  return <Outlet />
}
