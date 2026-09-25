import { useState } from 'react'
import Icon from './Icon'

/** Immagine di un'auto con segnaposto elegante se manca o non si carica (gli URL sono esterni). */
export default function CarImage({ src, alt, className }: { src: string | null | undefined; alt: string; className?: string }) {
  const [rotta, setRotta] = useState<string | null>(null)

  if (!src || rotta === src) {
    return (
      <div className={`car-image-placeholder ${className ?? ''}`} role="img" aria-label={alt}>
        <Icon name="car" size={40} />
      </div>
    )
  }
  return <img src={src} alt={alt} loading="lazy" className={className} onError={() => setRotta(src)} />
}
