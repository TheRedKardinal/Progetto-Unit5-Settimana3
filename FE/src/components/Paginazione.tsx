import { Pagination } from 'react-bootstrap'

/** Paginazione 0-based (come PageResponse.page del backend), con finestra di pagine attorno alla corrente. */
export default function Paginazione({
  page,
  totalPages,
  onChange,
}: {
  page: number
  totalPages: number
  onChange: (page: number) => void
}) {
  if (totalPages <= 1) return null

  const finestra = 2
  const pagine: (number | 'gap')[] = []
  for (let i = 0; i < totalPages; i++) {
    if (i === 0 || i === totalPages - 1 || Math.abs(i - page) <= finestra) pagine.push(i)
    else if (pagine[pagine.length - 1] !== 'gap') pagine.push('gap')
  }

  return (
    <Pagination className="justify-content-center gap-1 mt-5 mb-0">
      <Pagination.Prev disabled={page === 0} onClick={() => onChange(page - 1)} aria-label="Pagina precedente" />
      {pagine.map((p, i) =>
        p === 'gap' ? (
          <Pagination.Ellipsis key={`gap-${i}`} disabled />
        ) : (
          <Pagination.Item key={p} active={p === page} onClick={() => onChange(p)}>
            {p + 1}
          </Pagination.Item>
        ),
      )}
      <Pagination.Next
        disabled={page >= totalPages - 1}
        onClick={() => onChange(page + 1)}
        aria-label="Pagina successiva"
      />
    </Pagination>
  )
}
