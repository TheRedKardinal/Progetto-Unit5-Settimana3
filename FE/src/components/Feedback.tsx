import type { ReactNode } from 'react'
import { Alert, Spinner } from 'react-bootstrap'
import { Link } from 'react-router-dom'

export function Loader({ label = 'Caricamento' }: { label?: string }) {
  return (
    <div className="d-flex flex-column align-items-center justify-content-center py-5 gap-3 text-secondary">
      <Spinner animation="border" role="status" size="sm" />
      <span className="eyebrow">{label}</span>
    </div>
  )
}

export function ErrorAlert({ message, onRetry }: { message: string | null; onRetry?: () => void }) {
  if (!message) return null
  return (
    <Alert variant="danger" className="d-flex align-items-center justify-content-between gap-3">
      <span>{message}</span>
      {onRetry && (
        <button type="button" className="btn btn-sm btn-outline-dark" onClick={onRetry}>
          Riprova
        </button>
      )}
    </Alert>
  )
}

export function EmptyState({ title, text, action }: { title: string; text?: string; action?: ReactNode }) {
  return (
    <div className="empty-state surface text-center">
      <h2 className="h3 mb-2">{title}</h2>
      {text && <p className="text-secondary mb-4">{text}</p>}
      {action}
    </div>
  )
}

export function PageHeader({ eyebrow, title, children }: { eyebrow?: string; title: string; children?: ReactNode }) {
  return (
    <div className="page-header d-flex flex-wrap align-items-end justify-content-between gap-3">
      <div>
        {eyebrow && <p className="eyebrow mb-2">{eyebrow}</p>}
        <h1 className="display-5 mb-0">{title}</h1>
      </div>
      {children}
    </div>
  )
}

export function BackLink({ to, children }: { to: string; children: ReactNode }) {
  return (
    <Link to={to} className="back-link">
      <span aria-hidden="true">←</span> {children}
    </Link>
  )
}
