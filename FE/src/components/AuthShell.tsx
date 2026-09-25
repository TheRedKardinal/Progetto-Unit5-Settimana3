import type { ReactNode } from 'react'
import { Col, Container, Row } from 'react-bootstrap'

/** Layout diviso delle pagine di autenticazione: pannello carbone a sinistra, form su bianco a destra. */
export default function AuthShell({
  eyebrow,
  title,
  subtitle,
  children,
}: {
  eyebrow: string
  title: string
  subtitle?: ReactNode
  children: ReactNode
}) {
  return (
    <Container className="section">
      <Row className="auth-shell g-0">
        <Col lg={5} className="auth-aside d-none d-lg-flex">
          <p className="eyebrow text-white-50 mb-0">Salone Auto</p>
          <blockquote className="auth-quote">
            L'eleganza
            <br />è nei <em>dettagli</em>.
          </blockquote>
          <p className="mb-0 text-white-50 small">Vetture selezionate, trattative trasparenti.</p>
        </Col>
        <Col lg={7} className="auth-main">
          <div className="auth-form">
            <p className="eyebrow mb-2">{eyebrow}</p>
            <h1 className="display-6 mb-2">{title}</h1>
            {subtitle && <p className="text-secondary mb-4">{subtitle}</p>}
            {!subtitle && <div className="mb-4" />}
            {children}
          </div>
        </Col>
      </Row>
    </Container>
  )
}
