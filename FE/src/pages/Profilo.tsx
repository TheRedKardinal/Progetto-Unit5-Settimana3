import { Col, Container, Row } from 'react-bootstrap'
import { Link } from 'react-router-dom'
import { authApi } from '../api'
import { ErrorAlert, Loader, PageHeader } from '../components/Feedback'
import { formatData } from '../utils/format'
import { useAsync } from '../utils/useAsync'

export default function Profilo() {
  const { data: user, error, loading, reload } = useAsync(authApi.me, 'me')

  return (
    <Container>
      <PageHeader eyebrow="Il tuo account" title="Profilo" />
      <ErrorAlert message={error} onRetry={reload} />
      {loading && !user && <Loader />}
      {user && (
        <Row className="g-4">
          <Col lg={8}>
            <div className="surface p-4 p-md-5">
              <div className="d-flex align-items-center gap-4 mb-5">
                <div className="avatar">
                  {user.nome.charAt(0)}
                  {user.cognome.charAt(0)}
                </div>
                <div>
                  <h2 className="h2 mb-1">
                    {user.nome} {user.cognome}
                  </h2>
                  <p className="text-secondary mb-0">@{user.username}</p>
                </div>
              </div>
              <dl className="spec-grid mb-0">
                <div>
                  <dt>Email</dt>
                  <dd>{user.email}</dd>
                </div>
                <div>
                  <dt>Ruolo</dt>
                  <dd>{user.ruoli.map((r) => (r === 'ADMIN' ? 'Amministratore' : 'Cliente')).join(', ')}</dd>
                </div>
                <div>
                  <dt>Cliente dal</dt>
                  <dd>{formatData(user.createdAt)}</dd>
                </div>
                <div>
                  <dt>Username</dt>
                  <dd>{user.username}</dd>
                </div>
              </dl>
            </div>
          </Col>
          <Col lg={4}>
            <div className="surface bg-carbone p-4 p-md-5 h-100 d-flex flex-column">
              <p className="eyebrow text-white-50">Preferiti</p>
              <h2 className="h3 mb-3">La tua selezione</h2>
              <p className="text-white-50 mb-4">
                Imposta una soglia di prezzo sulle vetture salvate: ti scriveremo quando scende sotto la cifra scelta.
              </p>
              <Link to="/preferiti" className="btn btn-ghost-light mt-auto align-self-start">
                Vai ai preferiti
              </Link>
            </div>
          </Col>
        </Row>
      )}
    </Container>
  )
}
