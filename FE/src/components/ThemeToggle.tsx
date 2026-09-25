import { useEffect, useState } from 'react'
import Icon from './Icon'

type Tema = 'light' | 'dark'

// Stessa chiave usata dallo script in index.html, che applica il tema prima del primo render
const CHIAVE = 'salone.tema'

const temaCorrente = (): Tema => (document.documentElement.getAttribute('data-bs-theme') === 'dark' ? 'dark' : 'light')

function sceltaSalvata(): Tema | null {
  try {
    const t = localStorage.getItem(CHIAVE)
    return t === 'light' || t === 'dark' ? t : null
  } catch {
    return null
  }
}

function applica(tema: Tema) {
  document.documentElement.setAttribute('data-bs-theme', tema)
}

/** Interruttore chiaro/scuro. Senza una scelta esplicita segue il tema del sistema operativo. */
export default function ThemeToggle() {
  const [tema, setTema] = useState<Tema>(temaCorrente)

  useEffect(() => {
    const media = window.matchMedia('(prefers-color-scheme: dark)')
    const onChange = (e: MediaQueryListEvent) => {
      if (sceltaSalvata()) return
      const t: Tema = e.matches ? 'dark' : 'light'
      applica(t)
      setTema(t)
    }
    media.addEventListener('change', onChange)
    return () => media.removeEventListener('change', onChange)
  }, [])

  const cambia = () => {
    const t: Tema = tema === 'dark' ? 'light' : 'dark'
    applica(t)
    try {
      localStorage.setItem(CHIAVE, t)
    } catch {
      // Storage non disponibile: il tema vale solo per questa visita
    }
    setTema(t)
  }

  const label = tema === 'dark' ? 'Passa al tema chiaro' : 'Passa al tema scuro'
  return (
    <button type="button" className="theme-toggle" onClick={cambia} aria-label={label} title={label}>
      <Icon name={tema === 'dark' ? 'sun' : 'moon'} size={18} />
    </button>
  )
}
