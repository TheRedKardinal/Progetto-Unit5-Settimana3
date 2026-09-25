import { useEffect } from 'react'
import { Outlet, useLocation } from 'react-router-dom'
import AppNavbar from './AppNavbar'
import Footer from './Footer'

export default function Layout() {
  const { pathname } = useLocation()

  // Ogni cambio pagina riparte dall'alto (i cambi di sola query string, es. filtri, no)
  useEffect(() => {
    window.scrollTo({ top: 0 })
  }, [pathname])

  return (
    <>
      <AppNavbar />
      <main key={pathname} className="page-enter">
        <Outlet />
      </main>
      <Footer />
    </>
  )
}
