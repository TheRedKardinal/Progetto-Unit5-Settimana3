import { Col, Container, Row } from 'react-bootstrap'
import { Link } from 'react-router-dom'

export default function Footer() {
  return (
    <footer className="app-footer">
      <Container>
        <Row className="gy-4 align-items-end">
          <Col md={6}>
            <div className="brand mb-2">
              Salone <em>Auto</em>
            </div>
            <p className="mb-0 footer-muted">Vetture nuove, km 0 e usate, selezionate una a una.</p>
          </Col>
          <Col md={6} className="text-md-end">
            <nav className="d-flex gap-4 justify-content-md-end mb-2">
              <Link to="/">Catalogo</Link>
              <Link to="/preferiti">Preferiti</Link>
              <Link to="/profilo">Profilo</Link>
            </nav>
            <p className="mb-0 footer-muted small">© {new Date().getFullYear()} Salone Auto</p>
          </Col>
        </Row>
      </Container>
    </footer>
  )
}
