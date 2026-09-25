import { Route, Routes } from 'react-router-dom'
import { GuestOnly, RequireAuth } from './components/Guards'
import Layout from './components/Layout'
import Login from './pages/Login'
import NotFound from './pages/NotFound'
import PasswordDimenticata from './pages/PasswordDimenticata'
import Profilo from './pages/Profilo'
import Registrati from './pages/Registrati'
import ResetPassword from './pages/ResetPassword'
import VerificaEmail from './pages/VerificaEmail'

export default function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
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
        </Route>

        <Route path="*" element={<NotFound />} />
      </Route>
    </Routes>
  )
}
