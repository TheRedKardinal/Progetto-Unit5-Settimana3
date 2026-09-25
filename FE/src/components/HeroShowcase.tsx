import { useEffect, useState } from 'react'

/** Foto Unsplash (licenza Unsplash: uso libero, attribuzione non obbligatoria). pos = punto focale per object-position. */
const SLIDES = [
  { id: '1503376780353-7e6692767b70', nome: 'Porsche Panamera', pos: '62% 55%' },
  { id: '1544636331-e26879cd4d9b', nome: 'Bugatti Chiron', pos: '50% 60%' },
  { id: '1618843479313-40f8afb4b4d8', nome: 'Mercedes-AMG GT R', pos: '55% 60%' },
  { id: '1603584173870-7f23fdae1b7a', nome: 'Audi R8', pos: '55% 55%' },
  { id: '1614200179396-2bdb77ebf81b', nome: 'Ferrari F8 Tributo', pos: '55% 60%' },
  { id: '1583121274602-3e2820c69888', nome: 'Ferrari LaFerrari', pos: '60% 60%' },
  { id: '1542362567-b07e54358753', nome: 'McLaren 720S', pos: '50% 60%' },
]

const DURATA_MS = 7000

const url = (id: string, w: number) => `https://images.unsplash.com/photo-${id}?w=${w}&q=70&auto=format&fit=crop`

const movimentoRidotto = () => window.matchMedia('(prefers-reduced-motion: reduce)').matches

/**
 * Sfondo animato dell'hero: inquadrature in bianco e nero che si alternano con dissolvenza e un lento
 * zoom/panoramica. Con "riduci movimento" attivo niente scorrimento automatico (si cambia dagli indicatori).
 * Ogni foto viene scaricata solo quando sta per comparire.
 */
export default function HeroShowcase() {
  const [attiva, setAttiva] = useState(0)
  const [precedente, setPrecedente] = useState<number | null>(null)
  const [caricate, setCaricate] = useState(() => new Set([0, 1]))
  const [autoplay] = useState(() => !movimentoRidotto())

  const vai = (i: number) => {
    if (i === attiva) return
    setPrecedente(attiva)
    setAttiva(i)
    // Precarica la successiva, così la dissolvenza non parte su un'immagine vuota
    setCaricate((s) => new Set(s).add(i).add((i + 1) % SLIDES.length))
  }

  // Il timer riparte a ogni cambio (anche manuale), così ogni foto resta a schermo per la durata intera
  useEffect(() => {
    if (!autoplay) return
    const t = window.setTimeout(() => vai((attiva + 1) % SLIDES.length), DURATA_MS)
    return () => window.clearTimeout(t)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [attiva, autoplay])

  return (
    <>
      <div className="hero-media" aria-hidden="true">
        {SLIDES.map((s, i) =>
          caricate.has(i) ? (
            <img
              key={s.id}
              className={`hero-slide${i === attiva ? ' is-active' : ''}${i === precedente ? ' is-leaving' : ''}`}
              src={url(s.id, 1920)}
              srcSet={`${url(s.id, 1280)} 1280w, ${url(s.id, 1920)} 1920w, ${url(s.id, 2560)} 2560w`}
              sizes="100vw"
              alt=""
              style={{ objectPosition: s.pos }}
              decoding="async"
              fetchPriority={i === 0 ? 'high' : 'low'}
            />
          ) : null,
        )}
        <div className="hero-scrim" />
      </div>

      <div className="hero-caption">
        <span className="hero-caption-name">{SLIDES[attiva].nome}</span>
        <div className="hero-dots" role="group" aria-label="Foto dell'hero">
          {SLIDES.map((s, i) => (
            <button
              key={s.id}
              type="button"
              className={`hero-dot${i === attiva ? ' is-active' : ''}`}
              onClick={() => vai(i)}
              aria-label={`Mostra ${s.nome}`}
              aria-current={i === attiva}
            >
              {/* key sulla barra: si rimonta a ogni cambio e l'avanzamento riparte da zero */}
              {i === attiva && <span key={attiva} className={`hero-dot-fill${autoplay ? ' is-running' : ''}`} />}
            </button>
          ))}
        </div>
        <span className="hero-credit">Foto Unsplash</span>
      </div>
    </>
  )
}
