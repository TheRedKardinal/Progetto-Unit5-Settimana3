import { Container } from 'react-bootstrap'
import { Link } from 'react-router-dom'

export default function NotFound() {
  return (
    <Container className="section text-center">
      <p className="eyebrow mb-3">Errore 404</p>
      <h1 className="display-3 mb-3">Strada senza uscita</h1>
      <p className="text-secondary mb-4">La pagina che cerchi non esiste o è stata spostata.</p>
      <Link to="/" className="btn btn-primary">
        Torna al catalogo
      </Link>
    </Container>
  )
}
