import { Container, Nav, Navbar, NavDropdown } from 'react-bootstrap'
import { Link, NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/useAuth'
import Icon from './Icon'

export default function AppNavbar() {
  const { user, isAdmin, logout } = useAuth()
  const navigate = useNavigate()

  const esci = () => {
    logout()
    navigate('/')
  }

  return (
    <Navbar expand="lg" variant="dark" className="app-navbar" sticky="top" collapseOnSelect>
      <Container>
        <Navbar.Brand as={Link} to="/" className="brand">
          Salone <em>Auto</em>
        </Navbar.Brand>
        <Navbar.Toggle aria-controls="nav-principale" />
        <Navbar.Collapse id="nav-principale">
          <Nav className="me-auto ms-lg-5">
            <Nav.Link as={NavLink} to="/" end eventKey="catalogo">
              Catalogo
            </Nav.Link>
            {user && (
              <Nav.Link as={NavLink} to="/preferiti" eventKey="preferiti">
                Preferiti
              </Nav.Link>
            )}
            {isAdmin && (
              <Nav.Link as={NavLink} to="/admin" eventKey="admin">
                Gestione annunci
              </Nav.Link>
            )}
          </Nav>
          <Nav className="align-items-lg-center gap-2">
            {user ? (
              <NavDropdown
                align="end"
                id="menu-utente"
                title={
                  <span className="d-inline-flex align-items-center gap-2">
                    <Icon name="user" size={18} />
                    {user.nome}
                  </span>
                }
              >
                <NavDropdown.Item as={Link} to="/profilo">
                  Profilo
                </NavDropdown.Item>
                <NavDropdown.Item as={Link} to="/preferiti">
                  Preferiti
                </NavDropdown.Item>
                <NavDropdown.Divider />
                <NavDropdown.Item onClick={esci}>Esci</NavDropdown.Item>
              </NavDropdown>
            ) : (
              <>
                <Nav.Link as={NavLink} to="/login" eventKey="login">
                  Accedi
                </Nav.Link>
                <Link to="/registrati" className="btn btn-ghost-light btn-sm ms-lg-2">
                  Registrati
                </Link>
              </>
            )}
          </Nav>
        </Navbar.Collapse>
      </Container>
    </Navbar>
  )
}
