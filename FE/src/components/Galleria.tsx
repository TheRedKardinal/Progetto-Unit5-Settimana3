import { useState } from 'react'
import { Carousel } from 'react-bootstrap'
import CarImage from './CarImage'

/** Galleria del dettaglio: immagine principale (carousel) + miniature cliccabili. */
export default function Galleria({ immagini, alt }: { immagini: string[]; alt: string }) {
  const [indice, setIndice] = useState(0)

  if (immagini.length === 0) {
    return (
      <div className="gallery-main">
        <CarImage src={null} alt={alt} />
      </div>
    )
  }

  return (
    <div className="gallery">
      <Carousel
        activeIndex={indice}
        onSelect={setIndice}
        interval={null}
        indicators={immagini.length > 1}
        controls={immagini.length > 1}
        className="gallery-main"
        touch
      >
        {immagini.map((src, i) => (
          <Carousel.Item key={`${src}-${i}`}>
            <CarImage src={src} alt={`${alt} – foto ${i + 1}`} />
          </Carousel.Item>
        ))}
      </Carousel>
      {immagini.length > 1 && (
        <div className="gallery-thumbs">
          {immagini.map((src, i) => (
            <button
              key={`${src}-${i}`}
              type="button"
              className={`gallery-thumb${i === indice ? ' is-active' : ''}`}
              onClick={() => setIndice(i)}
              aria-label={`Mostra foto ${i + 1}`}
              aria-current={i === indice}
            >
              <CarImage src={src} alt="" />
            </button>
          ))}
        </div>
      )}
    </div>
  )
}
