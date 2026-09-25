import { Route, Routes } from 'react-router-dom'
import { GuestOnly, RequireAdmin, RequireAuth } from './components/Guards'
import Layout from './components/Layout'
import AdminAnnunci from './pages/admin/AdminAnnunci'
import AdminFormAnnuncio from './pages/admin/AdminFormAnnuncio'
import Catalogo from './pages/Catalogo'
import Dettaglio from './pages/Dettaglio'
import Login from './pages/Login'
import NotFound from './pages/NotFound'
import PasswordDimenticata from './pages/PasswordDimenticata'
import Preferiti from './pages/Preferiti'
import Profilo from './pages/Profilo'
import Registrati from './pages/Registrati'
import ResetPassword from './pages/ResetPassword'
import VerificaEmail from './pages/VerificaEmail'

export default function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route index element={<Catalogo />} />
        <Route path="/auto/:id" element={<Dettaglio />} />

        {/* Link inviati via email dal backend (EmailService): percorsi da non cambiare */}
        <Route path="/verifica-email" element={<VerificaEmail />} />
        <Route path="/reset-password" element={<ResetPassword />} />

        <Route path="/login" element={<Login />} />
        <Route element={<GuestOnly />}>
          <Route path="/registrati" element={<Registrati />} />
          <Route path="/password-dimenticata" element={<PasswordDimenticata />} />
        </Route>

        <Route element={<RequireAuth />}>
          <Route path="/profilo" element={<Profilo />} />
          <Route path="/preferiti" element={<Preferiti />} />
        </Route>

        <Route element={<RequireAdmin />}>
          <Route path="/admin" element={<AdminAnnunci />} />
          <Route path="/admin/auto/nuova" element={<AdminFormAnnuncio />} />
          <Route path="/admin/auto/:id" element={<AdminFormAnnuncio />} />
        </Route>

        <Route path="*" element={<NotFound />} />
      </Route>
    </Routes>
  )
}
